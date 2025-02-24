from ast import If
import torch
import random
import collections
import numpy as np
import networkx as nx
from src import utils
from src.clustergcn import ClusterGCNTrainer
from src.community import LocalCommunity
from src.recommend import PPRRecommend
import time
import datetime


class SubGraph(object):
    def __init__(self, args, graph, features, target, authorname, keywords):
        self.args = args
        self.graph = graph
        self.features = features
        self.target = target
        self.authorname = authorname
        self.keywords = keywords
        self._set_sizes()
        self.methods = {}
        self.device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
        self.time_map = {}
        self.rankloss = 0
        self.posforrank = []
        self.negforrank = []
        self.recnodes = {}
        self.cntmap = {}
        self.rec_eva = {}
        self.rec_evauser = {}
        self.rec_cnt = {}
        self.collect_vset = set()

    def _set_sizes(self):
        self.feature_count = self.features.shape[1]
        self.class_count = 2
        self.clusters = [0]
        self.cluster_membership = {node: 0 for node in self.graph.nodes()}

    def build_local_candidate_iteration(self, seed, round):
        '''
        Build subgraphs with iteration
        '''
        allNodes = []
        length = self.args.subgraph_size
        if (len(self.allnode) == 1):
            allNodes.append(seed)
            numLabel = int(length * self.args.train_ratio / 2)
            pos = 0
            while pos < len(allNodes) and pos < length and len(allNodes) < length:
                cnode = allNodes[pos]
                for nb in self.graph.neighbors(cnode):
                    if nb not in allNodes and len(allNodes) < length:
                        allNodes.append(nb)
                pos = pos + 1
        else:
            numLabel = self.args.possize
            allNodes = self.allnode[:]
        # print("The seed is %d" % seed)
        # print("The length of list is %d" % len(allNodes))
        # print("The degree of seed is %d" % self.graph.degree(seed))
        # '''Original expansion'''
        # if (self.args.recommend == 0 and round > 0):
            # for i in self.oldpos:
        
        for i in self.oldpos:
            if i not in allNodes:
                allNodes.append(i)
                print("pos not in subgraph")
            cnt = self.args.upsize
            for nb in self.graph.neighbors(i):
                if (cnt <= 0): break
                if (nb not in allNodes):
                    allNodes.append(nb)
                    cnt -= 1
            for nb in self.graph.neighbors(i):
                for nbnb in self.graph.neighbors(nb):
                    if (cnt <= 0): break
                    if (nbnb not in allNodes[:]):
                        allNodes.append(nbnb)
                        cnt -= 1
            
        
            # allNodes = self.oldres
            # pos = 0
            # while pos < len(allNodes) and pos < length and len(allNodes) < length:
            #     cnode = allNodes[pos]
            #     for nb in self.graph.neighbors(cnode):
            #         if nb not in allNodes and len(allNodes) < length:
            #             allNodes.append(nb)
            #     pos = pos + 1
        # if (self.args.recommend == 1):	
        # '''Two level expansion'''
        # if (self.args.recommend == 1 and round > 0):		
        #     for i in self.oldpos:
        #         cnt = self.args.upsize
        #         for nb in self.graph.neighbors(i):
        #             for nbnb in self.graph.neighbors(nb):
        #                 if (cnt <= 0): break
        #                 if (nbnb not in allNodes[:]):
        #                     allNodes.append(nbnb)
        #                     cnt -= 1            

        self.allnode = allNodes
        # print("The length of list is %d" % len(allNodes))
        # print([item for item, count in collections.Counter(self.allnode[:]).items() if count > 1])
        
        seedLabel = self.target[seed]

        '''
        Choose correct node only
        '''
        recOK = 0
        # print("The original length of posNodes is %d" % len(self.oldpos))
        # print("The original length of negNodes is %d" % len(self.oldneg))
        if (round > 0):
            rec = PPRRecommend(self.args, self)
            if (self.args.recommend == 0):
                recOK = rec.random_recommend(seed, round)
            elif (self.args.recommend == 1):
                recOK = rec.ppr_recommend(seed, round)
            if self.args.data_set == 'dblpname':
                self.output_subgraph(seed, self.collect_vset, "recommend_"+str(self.args.recommend)+"_round_"+str(round)+"_rec")
        # print("The after length of posNodes is %d" % len(self.oldpos))
        # print("The after length of negNodes is %d" % len(self.oldneg))
        # if recOK == 1:
        #     #this means there is a negNode is removed
        #     #we change the subgraph size
        #     allNodes = self.oldres

        if recOK == 2 and self.args.recommend ==1 and round>0:
            newpos = self.oldpos[-1]
            if (newpos not in allNodes[:]):
                allNodes.append(i)
                print("newpos not in subgraph")
            cnt = self.args.upsize
            for nb in self.graph.neighbors(newpos):
                if (cnt <= 0): break
                if (nb not in allNodes[:]):
                    allNodes.append(nb)
                    cnt -= 1
            for nb in self.graph.neighbors(newpos):
                for nbnb in self.graph.neighbors(nb):
                    if (cnt <= 0): break
                    if (nbnb not in allNodes[:]):
                        allNodes.append(nbnb)
                        cnt -= 1 
            self.allnode = allNodes

        # print("Positive Nodes are ")
        # for i in self.oldpos:
        #     print(self.authorname[i])
        # print("Negative Nodes are ")
        # for i in self.oldneg:
        #     print(self.authorname[i])

        
        # trainpossiz = int(self.cmty_siz/2)
        # trainnegsiz = trainpossiz
        # random.shuffle(self.oldpos)
        posNodes = []
        for i in self.oldpos:
            # if(trainpossiz == 0):
            #     break
            if (i in allNodes):
                posNodes.append(i)
                # trainpossiz = trainpossiz - 1
            # else:
                # allNodes.append(i)
                # print('pos不在里面')
        # random.shuffle(self.oldneg)
        negNodes = []
        for i in self.oldneg:
            # if(trainnegsiz == 0):
            #     break
            if (i in allNodes):
                negNodes.append(i)
                # trainnegsiz = trainnegsiz - 1
            # else:
                # allNodes.append(i)
                # print('neg不在里面')
        
        self.sg_nodes = {}
        self.sg_edges = {}
        self.sg_train_nodes = {}
        self.sg_test_nodes = {}
        self.sg_features = {}
        self.sg_targets = {}

        self.subgraph = nx.Graph()
        for i in range(len(allNodes)):
            for j in range(i):
                if ((allNodes[i], allNodes[j]) in self.graph.edges) or ((allNodes[j], allNodes[i]) in self.graph.edges):
                    self.subgraph.add_edge(allNodes[i], allNodes[j])

        # print("size of nodes %d size of edges %d" % (len(self.subgraph.nodes), len(self.subgraph.edges)))
        self.sg_nodes[0] = [node for node in sorted(self.subgraph.nodes())]
        self.sg_predProbs = [0.0] * len(self.sg_nodes[0])
        self.sg_predLabels = [0] * len(self.sg_nodes[0])
        self.mapper = {node: i for i, node in enumerate(sorted(self.sg_nodes[0]))}
        self.rmapper = {i: node for i, node in enumerate(sorted(self.sg_nodes[0]))}
        self.sg_edges[0] = [[self.mapper[edge[0]], self.mapper[edge[1]]] for edge in self.subgraph.edges()] + [
            [self.mapper[edge[1]], self.mapper[edge[0]]] for edge in self.subgraph.edges()]
        self.sg_posNodes = [self.mapper[node] for node in posNodes]
        self.sg_negNodes = [self.mapper[node] for node in negNodes]
        allNodes1 = [self.mapper[node] for node in allNodes]
        self.sg_train_nodes[0] = self.sg_posNodes + self.sg_negNodes
        self.sg_test_nodes[0] = list(set(allNodes1).difference(set(self.sg_train_nodes[0])))
        self.sg_test_nodes[0] = sorted(self.sg_test_nodes[0])
        self.sg_train_nodes[0] = sorted(self.sg_train_nodes[0])
        self.sg_features[0] = self.features[self.sg_nodes[0], :]
        self.sg_targets[0] = self.target[self.sg_nodes[0], :]
        self.sg_targets[0] = self.sg_targets[0] == seedLabel
        self.sg_targets[0] = self.sg_targets[0].astype(int)


        # print("Value 0 %d, Value 1 %d" % (sum(self.sg_targets[0] == 0), sum(self.sg_targets[0] == 1)))
        for x in self.sg_posNodes:
            self.sg_predProbs[x] = 1.0
            self.sg_predLabels[x] = 1
            # if self.sg_targets[0][x] != 1.0:
                #self.sg_targets[0][x] = 1.0
            # if self.sg_targets[0][x] != 1.0:
                # print("wrong1")
        for x in self.sg_negNodes:
            self.sg_predProbs[x] = 0.0
            self.sg_predLabels[x] = 0
            # if self.sg_targets[0][x] != 0:
            #     print("wrong0")

        self.transfer_edges_and_nodes()
        if (self.args.recommend == 0):
            self.TOPK_SIZE = int(self.args.community_size)

        if (self.args.recommend == 1):
            self.TOPK_SIZE = int(self.cmty_siz)
        
        return 1
            
    def build_local_candidate(self, seed, trian_node, label):
        '''
        Build subgraphs
        '''
        allNodes = []
        allNodes.append(seed)
        posNodes = set()
        negNodes = set()

        length = self.args.subgraph_size
        numLabel = int(length * self.args.train_ratio / 2)
        pos = 0
        while pos < len(allNodes) and pos < length and len(allNodes) < length:
            cnode = allNodes[pos]
            for nb in self.graph.neighbors(cnode):
                if nb not in allNodes and len(allNodes) < length:
                    allNodes.append(nb)
                    if(nb!=seed and self.target is not None):
                        if(self.target[nb] == self.target[seed]):
                            posNodes.add(nb)
                        else:
                            negNodes.add(nb)
            pos = pos + 1
        posNodes=list(posNodes)
        negNodes=list(negNodes)
        # print("The length of list is %d" % len(allNodes))
        # print("The degree of seed is %d" % self.graph.degree(seed))
        if (trian_node is not None):
            posNodes = trian_node[:numLabel]
            negNodes = trian_node[numLabel:]
        else:
            seedLabel = self.target[seed]
            posNodes.append(seed)
            if(len(posNodes+[seed])<numLabel or len(negNodes)<numLabel):
                return 0
            random.shuffle(posNodes)
            random.shuffle(negNodes)
            posNodes=[seed]+posNodes[:numLabel-1]
            negNodes=negNodes[:numLabel]
        # print("Positive Nodes are ")
        # print(posNodes)
        # print("Negative Nodes are ")
        # print(negNodes)


        self.sg_nodes = {}
        self.sg_edges = {}
        self.sg_train_nodes = {}
        self.sg_test_nodes = {}
        self.sg_features = {}
        self.sg_targets = {}

        self.subgraph = nx.Graph()
        for i in range(len(allNodes)):
            for j in range(i):
                if ((allNodes[i], allNodes[j]) in self.graph.edges) or ((allNodes[j], allNodes[i]) in self.graph.edges):
                    self.subgraph.add_edge(allNodes[i], allNodes[j])

        # print("size of nodes %d size of edges %d" % (len(self.subgraph.nodes), len(self.subgraph.edges)))
        self.sg_nodes[0] = [node for node in sorted(self.subgraph.nodes())]
        self.sg_predProbs = [0.0] * len(self.sg_nodes[0])
        self.sg_predLabels = [0] * len(self.sg_nodes[0])
        self.mapper = {node: i for i, node in enumerate(sorted(self.sg_nodes[0]))}
        self.rmapper = {i: node for i, node in enumerate(sorted(self.sg_nodes[0]))}
        self.sg_edges[0] = [[self.mapper[edge[0]], self.mapper[edge[1]]] for edge in self.subgraph.edges()] + [
            [self.mapper[edge[1]], self.mapper[edge[0]]] for edge in self.subgraph.edges()]
        self.sg_posNodes = [self.mapper[node] for node in posNodes]
        self.sg_negNodes = [self.mapper[node] for node in negNodes]

        allNodes1 = [self.mapper[node] for node in allNodes]
        self.sg_train_nodes[0] = self.sg_posNodes + self.sg_negNodes
        self.sg_test_nodes[0] = list(set(allNodes1).difference(set(self.sg_train_nodes[0])))

        self.sg_test_nodes[0] = sorted(self.sg_test_nodes[0])
        self.sg_train_nodes[0] = sorted(self.sg_train_nodes[0])

        self.sg_features[0] = self.features[self.sg_nodes[0], :]
        if (label is None):
            self.sg_targets[0] = self.target[self.sg_nodes[0], :]
            self.sg_targets[0] = self.sg_targets[0] == seedLabel
            self.sg_targets[0] = self.sg_targets[0].astype(int)
        else:
            self.sg_targets[0] = [0] * len(allNodes1)
            for i in label:
                self.sg_targets[0][self.mapper[i]] = 1
            self.sg_targets[0] = np.array(self.sg_targets[0])
            self.sg_targets[0] = self.sg_targets[0][:, np.newaxis]
            self.sg_targets[0] = self.sg_targets[0].astype(int)

        # print("Value 0 %d, Value 1 %d" % (sum(self.sg_targets[0] == 0), sum(self.sg_targets[0] == 1)))
        for x in self.sg_posNodes:
            self.sg_predProbs[x] = 1.0
            self.sg_predLabels[x] = 1
            if self.sg_targets[0][x] != 1.0:
                print("wrong")
        for x in self.sg_negNodes:
            self.sg_predProbs[x] = 0.0
            self.sg_predLabels[x] = 0
            if self.sg_targets[0][x] != 0:
                print("wrong")

        self.transfer_edges_and_nodes()
        self.TOPK_SIZE = self.args.community_size


    def transfer_edges_and_nodes(self):
        '''
        Transfering the data to PyTorch format.
        '''
        for cluster in self.clusters:
            self.sg_nodes[cluster] = torch.LongTensor(self.sg_nodes[cluster]).to(self.device)
            self.sg_edges[cluster] = torch.LongTensor(self.sg_edges[cluster]).t().to(self.device)
            self.sg_train_nodes[cluster] = torch.LongTensor(self.sg_train_nodes[cluster]).to(self.device)
            self.sg_test_nodes[cluster] = torch.LongTensor(self.sg_test_nodes[cluster]).to(self.device)
            self.sg_features[cluster] = torch.FloatTensor(self.sg_features[cluster]).to(self.device)
            self.sg_targets[cluster] = torch.LongTensor(self.sg_targets[cluster]).to(self.device)


    def community_search(self, seed, trian_node, label):
        '''
        GNN training subgraph, heuristic search community without/with rking loss
        '''
        self.rankloss = 0
        isOK = self.build_local_candidate(seed, trian_node, label)
        if isOK == 0:
            print("cannot build a local subgraph")
            return 0
        for round in range(2):
            keepLayers = self.args.layers.copy()
            gcn_trainer = ClusterGCNTrainer(self.args, self)
            begin_time = time.time()
            nodeweight, predlabels, f1score = gcn_trainer.train_test_community()
            if 'gcn' not in self.time_map:
                self.time_map['gcn'] = time.time() - begin_time
                self.cntmap['gcn']=1
            else:
                self.time_map['gcn'] = time.time() - begin_time + self.time_map['gcn']
                self.cntmap['gcn'] +=1
            self.args.layers = keepLayers
            lc = LocalCommunity(self.args, self)

            for i in range(len(self.sg_test_nodes[0])):
                self.sg_predProbs[self.sg_test_nodes[0][i]] = nodeweight[i].item()
                self.sg_predLabels[self.sg_test_nodes[0][i]] = predlabels[i].item()

            if(self.rankloss == 1):
                prefix='With rking loss'
            else:
                prefix="Without rking loss"


            begin_time = time.time()
            topk = lc.locate_community_BFS_only(seed)
            lc.evaluate_community(topk, prefix + " BSF Only",time.time() - begin_time)


            begin_time = time.time()
            topk = lc.locate_community_BFS(seed)
            lc.evaluate_community(topk, prefix + " BSF Swap", time.time() - begin_time)




            begin_time = time.time()
            topk = lc.locate_community_greedy(seed)
            lc.evaluate_community(topk, prefix + " Greedy-T", time.time() - begin_time)



            begin_time = time.time()
            topk = lc.locate_community_greedy_graph_prepath(seed)
            lc.evaluate_community(topk, prefix + " Greedy-G", time.time() - begin_time)

            self.posforrank, self.negforrank = self.getPNpairs()
            self.rankloss = 1


        return 1


    def getPNpairs(self):
        '''
        Get rking loss pair
        '''
        probs = self.sg_predProbs.copy()
        for x in self.sg_train_nodes[0]:
            probs[x] = 2
        for i in range(len(self.sg_targets[0])):
            if self.sg_targets[0][i] == 0:
                probs[i] = 2
        posIdx = np.argsort(np.array(probs))[0:int(self.args.train_ratio * self.args.subgraph_size / 2)]
        probs = self.sg_predProbs.copy()
        for x in self.sg_train_nodes[0]:
            probs[x] = -2
        for i in range(len(self.sg_targets[0])):
            if self.sg_targets[0][i] == 1:
                probs[i] = -2
        negIdx = np.argsort(-np.array(probs))[0:int(self.args.train_ratio * self.args.subgraph_size / 2)]
        return posIdx, negIdx
    
    def revise_final_ans(self, final_ans):
        print('Please choose the authors you want to remove from final answer:')
        revised_ans = {}
        for i in final_ans:
            print(str(final_ans.index(i)+1) + '. ' + self.authorname[i]+ ' '+self.keywords[i])
            revised_ans[final_ans.index(i)] = i
        index_list = []
        revised_ans_list = []
        for i in range(len(final_ans)):
            select_aut = int(input('Enter number to select, enter 0 if you want to finish:'))-1
            if select_aut < 0:
                break
            else:
                index_list.append(select_aut)
        
        for i in range(len(index_list)):
            revised_ans.pop(index_list[i])
        revised_ans_list = list(revised_ans.values())
            
        return revised_ans_list

    def check_path_to_subgraph(self, graph, start, subgraph_nodes):
        
        visited = {node: False for node in graph.nodes()}
        queue = [start]

        while queue:
            current = queue.pop(0)
            if not visited[current]:
                visited[current] = True
                # If the current node is in the subgraph, return the path
                if current in subgraph_nodes:
                    return current
                for neighbor in graph.neighbors(current):
                    if not visited[neighbor]:
                        queue.append(neighbor)
        return None

    def output_subgraph(self, qid, target_vertices, res_label):
        visited = set()
        print_vertices = set()
        result_edges = set()

        visited.add(qid)
        for vertex in target_vertices:
            print_vertices.add(str(vertex) + "," + self.authorname[vertex]+ ','+self.keywords[vertex])
            for neighbor in self.graph.neighbors(vertex):
                if neighbor in target_vertices:
                    if vertex > neighbor:
                        vertex, neighbor = neighbor, vertex
                    result_edges.add(str(vertex) + "," + str(neighbor))

        self.write_to_file("_case_subvertices_" + self.authorname[qid] + ".csv", res_label, print_vertices)
        self.write_to_file("_case_subedges_" + self.authorname[qid] + ".csv", res_label, result_edges)

    def write_to_file(self, file_name, res_label, data):
        timestamp = datetime.datetime.now().strftime("%Y-%m-%d-%H-%M-%S")

        try:
            with open("./log/"+timestamp + "_" + res_label + file_name, 'a') as fw:
                if "subvertices" in file_name:
                    fw.write("Id,Name,Label\n")
                elif "subedges" in file_name:
                    fw.write("Source,Target\n")

                for item in data:
                    fw.write(item + "\n")

        except Exception as e:
            print(e)

    def community_search_iteration(self,seed,com_len,posinit,neginit, itr):
        '''
        GNN training subgraph, heuristic search community  with iteration without rking loss
        '''
        self.seed = seed
        self.com_len = com_len
        self.oldpos = posinit
        self.oldneg = neginit
        self.oldres = [seed]
        self.allnode = [seed]
        self.round = 0
        self.cmty_siz = self.args.community_size
        search_time= {}
        res_set = {}
        if self.args.data_set == 'dblpname':
            utils.log("Query No.: "+str(itr))
            utils.log("Query name:"+self.authorname[seed])
        for round in range(self.args.round):
            if round < self.round and self.args.data_set != 'dblpname':
                if round == self.args.round - 1 and self.args.recommend == 1:
                    siz_last = self.args.community_size
                    self.args.community_size = int((siz_last * itr + self.cmty_siz) / (itr + 1))
                begin_time = time.time()
                lc_1 = LocalCommunity(self.args, self)
                oldtopk = [self.mapper[idx] for idx in self.oldres]
                lc_1.evaluate_community(oldtopk[round], com_len, str(round)+' Round' + " Greedy-G", time.time() - begin_time)
                continue
            seed = self.seed
            com_len = self.com_len
            isOK = self.build_local_candidate_iteration(seed, round)
            if isOK == 0:
                print("cannot build a local subgraph")
                return 0
            if round == self.args.round - 1 and self.args.recommend == 1:
                siz_last = self.args.community_size
                self.args.community_size = int((siz_last * itr + self.cmty_siz) / (itr + 1))
            keepLayers = self.args.layers.copy()
            gcn_trainer = ClusterGCNTrainer(self.args, self)
            begin_time = time.time()
            nodeweight, predlabels, f1score = gcn_trainer.train_test_community()
            if 'gcn' not in self.time_map:
                self.time_map['gcn'] = time.time() - begin_time
                self.cntmap['gcn']=1
            else:
                self.time_map['gcn'] = time.time() - begin_time + self.time_map['gcn']
                self.cntmap['gcn'] +=1
            self.args.layers = keepLayers
            lc = LocalCommunity(self.args, self)

            for i in range(len(self.sg_test_nodes[0])):
                self.sg_predProbs[self.sg_test_nodes[0][i]] = nodeweight[i].item()
                self.sg_predLabels[self.sg_test_nodes[0][i]] = predlabels[i].item()

            prefix=str(round)+' Round'
            # begin_time = time.time()
            # topk = lc.locate_community_BFS_only(seed)
            # lc.evaluate_community(topk, com_len, prefix + " BSF Only",time.time() - begin_time)


            # begin_time = time.time()
            # topk = lc.locate_community_BFS(seed)
            # lc.evaluate_community(topk, com_len, prefix + " BSF Swap", time.time() - begin_time)




            # begin_time = time.time()
            # topk = lc.locate_community_greedy(seed)
            # lc.evaluate_community(topk, com_len, prefix + " Greedy-T", time.time() - begin_time)



            begin_time = time.time()
            topk = lc.locate_community_greedy_graph_prepath(seed)
            oldresCheck = [self.rmapper[idx] for idx in topk]

            # New to protect positive and ignore negative vertices
            if (self.args.recommend == 1):
                notinsert = [x for x in self.oldpos if x not in oldresCheck]
                pathset = []
                for begin in notinsert:
                    pathcheck = self.check_path_to_subgraph(self.subgraph, begin, oldresCheck)
                    if pathcheck != None:
                        newpathset = nx.shortest_path(self.subgraph, begin, pathcheck)
                        pathset.extend(x for x in newpathset if x not in pathset)
                oldresCheck.extend(x for x in pathset if x not in oldresCheck)
                self.oldres = [x for x in oldresCheck if x not in self.oldneg]
            else:
                self.oldres = oldresCheck

            res_set[round] = self.oldres
            search_time[round] = time.time() - begin_time
            finalans = []
            
            if self.args.data_set != 'dblpname':
                topk = [self.mapper[idx] for idx in self.oldres]
                lc.evaluate_community(topk, com_len, prefix + " Greedy-G", search_time[round])
                self.round = round + 1
            else:
                utils.printlog("Round "+str(round)+" Result community:")
                for i in self.oldres:
                    utils.printlog(str(self.oldres.index(i)+1) + '. ' + self.authorname[i]+ ' '+self.keywords[i])
                target_vertices = self.oldres
                self.output_subgraph(seed, target_vertices, "recommend_"+str(self.args.recommend)+"_round_"+str(round)+"_res")
                self.collect_vset.clear()
                self.collect_vset.update(target_vertices)
                self.round = round + 1
                if round < self.args.round-1:
                    cont = int(input("Press 1 to continue or press 0 to finish: "))
                    if cont == 0:
                        # revise final answer
                        finalans = self.revise_final_ans(self.oldres)
                        self.output_subgraph(seed, finalans, "recommend_"+str(self.args.recommend)+"_round_"+str(round)+"_final_res")
                        print("Evaluation is end, thank you for your participant.")
                        for i in range(round+1, self.args.round):
                            res_set[i] = res_set[round]
                            search_time[i] = search_time[round]
                        for i in range(round, self.args.round-1):
                            self.recnodes[i] = self.recnodes[round-1]
                            if i in self.rec_eva:
                                self.rec_eva[i] = [self.rec_eva[i][j]+self.rec_cnt[j] for j in range(len(self.rec_cnt))]
                            else:
                                self.rec_eva[i] = self.rec_cnt
        
                        lc.userstudy_evaluate(res_set, finalans, " Greedy-G", search_time, round)
                        return 1
                else:
                    print("Evaluation is end, thank you for your participant.")
        if self.args.data_set == 'dblpname':
            # revise final answer
            finalans = self.revise_final_ans(self.oldres)
            lc.userstudy_evaluate(res_set, finalans, " Greedy-G", search_time, self.args.round)

        return 1
    
    def user_study_eva(self, itr):
        '''
        save user study result
        '''
        now = datetime.datetime.now()
        sTime = now.strftime("%Y-%m-%d %H:%M:%S")
        file_handle = open('./data/results' + sTime + '.txt', mode='a+')
        file_handle.write(sTime + "\n")
        args = vars(self.args)
        keys = sorted(args.keys())
        keycontent = [[k.replace("_", " ").capitalize(), args[k]] for k in keys]
        for x in keycontent:
            file_handle.writelines(str(x) + "\n")
        file_handle.write("gcn time %f \n" % (self.time_map['gcn'] / self.cntmap['gcn']))
        print("gcn time %f " % (self.time_map['gcn'] / self.cntmap['gcn']))
        file_handle.write("Method,Avg Iteration, Precision,Recall,F1score,Time(s), \n" )

        for method in self.methods:
            if isinstance(self.methods[method], list):
                round = self.methods[method][0] / itr
                pre = self.methods[method][1] / itr
                rec= self.methods[method][2] / itr
                f1 = self.methods[method][3] / itr
                times = self.time_map[method] / itr
                print(
                    "%s Method achieve the average round= %f the average precision= %f recall= %f f1score= %f using %d seeds with avgtime=%f s " % (
                        method, round, pre, rec, f1, itr, times))
                file_handle.writelines(
                    "%s,%f,%f,%f,%f,%d,%f, \n" % (
                        method, round, pre, rec, f1, itr, times))
        file_handle.write("Round, Recommend time pos, Recommend time neg, Recommend precision pos,Recommend precision neg, Recommend target pos, Recommend target neg, \n" )
        for round in range(self.args.round - 1):
            pos_time = self.rec_eva[round][0] / itr
            neg_time = self.rec_eva[round][1] / itr
            pos_tar = self.rec_eva[round][2] / itr
            neg_tar = self.rec_eva[round][3] / itr
            pos_pre = self.rec_evauser[round][0] / itr
            neg_pre = self.rec_evauser[round][1] / itr
            file_handle.writelines(
                    "%d Round,%f,%f,%f,%f,%f,%f, \n" % (
                        round+1, pos_time, neg_time, pos_pre, neg_pre, pos_tar, neg_tar))
        file_handle.writelines("\n")
        file_handle.close()
    def methods_result(self):
        '''
        save result
        '''
        now = datetime.datetime.now()
        sTime = now.strftime("%Y-%m-%d %H:%M:%S")
        file_handle = open('./data/results' + sTime + '.txt', mode='a+')
        file_handle.write(sTime + "\n")
        args = vars(self.args)
        keys = sorted(args.keys())
        keycontent = [[k.replace("_", " ").capitalize(), args[k]] for k in keys]
        for x in keycontent:
            file_handle.writelines(str(x) + "\n")
        file_handle.write("gcn time %f \n" % (self.time_map['gcn'] / self.cntmap['gcn']))
        print("gcn time %f " % (self.time_map['gcn'] / self.cntmap['gcn']))
        file_handle.write("Method,Precision,Precision-without-posnode,Recall,Recall-without-posnode,F1score,F1score-without-posnode,Seeds,Time(s), \n" )

        for method in self.methods:
            if isinstance(self.methods[method], list):
                pre = self.methods[method][0] / self.cntmap[method]
                rpre= self.methods[method][1] / self.cntmap[method]
                rec = self.methods[method][2] / self.cntmap[method]
                rrec= self.methods[method][3] / self.cntmap[method]
                f1 = self.methods[method][4] / self.cntmap[method]
                rf1= self.methods[method][5] / self.cntmap[method]
                times = self.time_map[method] / self.cntmap[method]
                print(
                    "%s Method achieve the average precision= %f precision without posnode = %f recall= %f recall without posnode = %f f1score= %f f1score without posnode = %f using %d seeds with avgtime=%f s " % (
                        method, pre, rpre, rec, rrec, f1, rf1, self.cntmap[method], times))
                file_handle.writelines(
                    "%s,%f,%f,%f,%f,%f,%f,%d,%f, \n" % (
                        method, pre, rpre, rec, rrec, f1, rf1, self.cntmap[method], times))
        file_handle.write("Round, Recommend time pos, Recommend time neg, Recommend precision pos,Recommend precision neg, Recommend target pos, Recommend target neg, \n" )
        for round in range(self.args.round - 1):
            pos_time = self.rec_eva[round][0] / self.rec_cnt[round]
            neg_time = self.rec_eva[round][1] / self.rec_cnt[round]
            pos_pre = self.rec_eva[round][2] / self.rec_cnt[round]
            neg_pre = self.rec_eva[round][3] / self.rec_cnt[round]
            pos_tar = self.rec_eva[round][4] / self.rec_cnt[round]
            neg_tar = self.rec_eva[round][5] / self.rec_cnt[round]
            file_handle.writelines(
                    "%d Round,%f,%f,%f,%f,%f,%f, \n" % (
                        round+1, pos_time, neg_time, pos_pre, neg_pre, pos_tar, neg_tar))
        file_handle.writelines("\n")
        file_handle.close()
