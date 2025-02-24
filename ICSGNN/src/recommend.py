from heapq import heappop, heappush, heapify, nlargest, nsmallest
from collections import deque
import random
import networkx as nx
import time
from src import utils

class PPRRecommend(object):

    def __init__(self, args, upgraph):
        self.args = args
        self.upgraph = upgraph
        self.rec_res = []
    def userinter(self, seed, round, rec_pos_nodes, rec_neg_nodes):    
        posNodes = []
        negNodes = []
        numLabel = self.args.possize
        seedLabel = self.upgraph.target[seed]
        change_siz = int(self.upgraph.cmty_siz * 0.20)
        self.upgraph.collect_vset.update(rec_pos_nodes)
        self.upgraph.collect_vset.update(rec_neg_nodes)

        utils.printlog("Recommended authors for insertion: \n 0. No insertion.")
        for i, node in enumerate(rec_pos_nodes):
            utils.printlog(str(i+1)+'.'+self.upgraph.authorname[node]+ ' '+self.upgraph.keywords[node])
        chospos = int(input("Press number to choose the insertion author:"))-1

        utils.printlog("Recommended authors for deletion: \n 0. No deletion.")
        for i, node in enumerate(rec_neg_nodes):
            utils.printlog(str(i+1)+'.'+self.upgraph.authorname[node]+ ' '+self.upgraph.keywords[node])
        chosneg = int(input("Press number to choose the deletion author:"))-1

        if (chospos < 0):
            # self.upgraph.oldneg.extend(rec_pos_nodes)
            self.rec_res.append(0)
            utils.printlog("You choose 0. No insertion.")
        else:
            self.upgraph.oldpos.append(rec_pos_nodes[chospos])
            utils.printlog("You insert author:"+self.upgraph.authorname[rec_pos_nodes[chospos]])
            self.upgraph.cmty_siz = self.upgraph.cmty_siz + change_siz
            self.rec_res.append(1)
        
        if (chosneg < 0):
            # self.upgraph.oldpos.extend(rec_neg_nodes)
            self.rec_res.append(0)
            utils.printlog("You choose 0. No deletion.")
        else:
            self.upgraph.oldneg.append(rec_neg_nodes[chosneg])
            utils.printlog("You delete author:"+self.upgraph.authorname[rec_neg_nodes[chosneg]])
            self.upgraph.cmty_siz = self.upgraph.cmty_siz - change_siz
            self.rec_res.append(1)
        if round-1 in self.upgraph.rec_eva:
            self.upgraph.rec_eva[round-1] = [self.upgraph.rec_eva[round-1][i]+self.rec_res[i] for i in range(len(self.rec_res))]
        else:
            self.upgraph.rec_eva[round-1] = self.rec_res
        self.upgraph.rec_cnt = self.rec_res
        # print(" Recommend time pos={:.4f}s  neg={:.4f}s".format(self.rec_res[0],self.rec_res[1]))
        # print(" Recommend precision pos={:.4f}  neg={:.4f}".format(self.rec_res[2],self.rec_res[3]))
        # print(" Recommend target pos={:.4f}  neg={:.4f}".format(self.rec_res[2],self.rec_res[3]))
        return 1
    def evaluate_recommend(self, seed, round, rec_pos_nodes, rec_neg_nodes):    
        posNodes = []
        negNodes = []
        numLabel = self.args.possize
        seedLabel = self.upgraph.target[seed]
        change_siz = int(self.upgraph.cmty_siz * 0.1)
        isok = 1
        
        for node in rec_pos_nodes:
            if self.upgraph.target[node] == seedLabel:
                posNodes.append(node)
        for node in rec_neg_nodes:
            if self.upgraph.target[node] != seedLabel:
                negNodes.append(node)
      
        print('rec pos size', len(posNodes))
        print('rec neg size', len(negNodes))
        if len(rec_pos_nodes) > 0:
            self.rec_res.append(len(posNodes)/len(rec_pos_nodes))
        else:
            self.rec_res.append(0.0)
        if len(rec_neg_nodes) > 0:
            self.rec_res.append(len(negNodes)/len(rec_neg_nodes))
        else:
            self.rec_res.append(0.0)

        if (len(posNodes) < numLabel):
            print('e1')
            # self.upgraph.oldneg.extend(rec_pos_nodes)
            self.rec_res.append(0)
        else:
            posNodes = posNodes[:numLabel]
            self.rec_res.append(1)
        
        if (len(negNodes) < numLabel):
            print('e2')
            # self.upgraph.oldpos.extend(rec_neg_nodes)
            self.rec_res.append(0)
        else:
            negNodes = negNodes[:numLabel]
            self.rec_res.append(1)

        if (len(posNodes) == numLabel and len(negNodes) == numLabel):
            r = random.randint(0,1)
            if (r < 0.5):
                self.upgraph.oldpos.extend(posNodes)
                self.upgraph.cmty_siz = self.upgraph.cmty_siz + change_siz
                isok = 2
            else:
                self.upgraph.oldneg.extend(negNodes)
                self.upgraph.cmty_siz = self.upgraph.cmty_siz - change_siz
                isok = 3
        elif (len(posNodes) == numLabel):
            self.upgraph.oldpos.extend(posNodes)
            self.upgraph.cmty_siz = self.upgraph.cmty_siz + change_siz
            isok = 2
        elif (len(negNodes) == numLabel):
            self.upgraph.oldneg.extend(negNodes)
            self.upgraph.cmty_siz = self.upgraph.cmty_siz - change_siz
            isok = 3
            
        if round-1 in self.upgraph.rec_eva:
            self.upgraph.rec_cnt[round-1] = self.upgraph.rec_cnt[round-1] + 1
            self.upgraph.rec_eva[round-1] = [self.upgraph.rec_eva[round-1][i]+self.rec_res[i] for i in range(len(self.rec_res))]
        else:
            self.upgraph.rec_cnt[round-1] = 1
            self.upgraph.rec_eva[round-1] = self.rec_res
        print(" Recommend time pos={:.4f}s  neg={:.4f}s".format(self.rec_res[0],self.rec_res[1]))
        print(" Recommend precision pos={:.4f}  neg={:.4f}".format(self.rec_res[2],self.rec_res[3]))
        print(" Recommend target pos={:.4f}  neg={:.4f}".format(self.rec_res[4],self.rec_res[5]))
            
        return isok
    def ppr_algo(self, tag, allResNodes, allNodes):
        '''
        Personalized Pagerank recommend posnodes and negnodes
        '''
        # for very small candidate
        if len(allNodes)-len(allResNodes) < 11:
            begin_time = time.time()
            rec_all_candidate = [x for x in allNodes if x not in allResNodes]
            rec_time = time.time() - begin_time
            self.rec_res.append(rec_time)
            return rec_all_candidate
        
        sg_nodes = {}
        sg_edges = {}
        
        subgraph = nx.Graph()
        subgraph.add_nodes_from(allNodes)
        for i in range(len(allNodes)):
            for j in range(i):
                if ((allNodes[i], allNodes[j]) in self.upgraph.graph.edges) or ((allNodes[j], allNodes[i]) in self.upgraph.graph.edges):
                    subgraph.add_edge(allNodes[i], allNodes[j])

        # print("size of nodes %d size of edges %d" % (len(subgraph.nodes), len(subgraph.edges)))
        sg_nodes[0] = [node for node in sorted(subgraph.nodes())]
        mapper = {node: i for i, node in enumerate(sorted(sg_nodes[0]))}
        rmapper = {i: node for i, node in enumerate(sorted(sg_nodes[0]))}
        sg_edges[0] = [[mapper[edge[0]], mapper[edge[1]]] for edge in subgraph.edges()]
        sg_allResNodes = [mapper[node] for node in allResNodes]
        sg_allNodes = [mapper[node] for node in allNodes]
        
        begin_time = time.time()
        # Mark all the vertices as not visited
        # visited = [False] * (len(allNodes))
        level = [-1] * (len(allNodes))
        page_rank = [0.0] * (len(allNodes))
        # Create a queue for BFS
        queue = deque()
        can_pos_nodes = set()
        visited = set(sg_allResNodes)
 
        # Mark the source node as
        # visited and enqueue it
        for i in sg_allResNodes[:]:
            # visited[i] = True
            level[i] = 0
            queue.append(i)
        
        '''Directed Graph part'''
        level_max = 0
        while queue:
 
            # Dequeue a vertex from
            # queue and print it
            s = queue.popleft()
            level_now = level[s] + 1

            # Get all adjacent vertices of the
            # dequeued vertex s. If a adjacent
            # has not been visited, then mark it
            # visited and enqueue it
            for nb in subgraph.neighbors(rmapper[s]):
                sg_nb = mapper[nb]
                if sg_nb not in visited:
                    can_pos_nodes.add(sg_nb)
                    queue.append(sg_nb)
                    visited.add(sg_nb)
                    # visited[sg_nb] = True
                    level[sg_nb] = level_now
                    if level_now>level_max:
                        level_max = level_now

        DG = nx.DiGraph()
        DG.add_nodes_from(sg_allNodes)
        for [i, j] in sg_edges[0]:
            if level[i] > level[j]:
                DG.add_edge(j, i)
            elif level[i] < level[j]:
                DG.add_edge(i, j)
            elif level[i] > 0:
                DG.add_edge(i, j)
                DG.add_edge(j, i)
        
        '''PageRank part'''
            
        damping_factor = 0.85
        max_iterations = level_max + 1
        min_delta = 0.0001
        # damping_value = round((1.0 - damping_factor) / len(allNodes), 6)
        damping_value = (1.0 - damping_factor) / (len(allNodes)-len(allResNodes)+1)
        out_deg = DG.out_degree()
        sumRootOutNbSiz = 0
        for i in sg_allResNodes:
            sumRootOutNbSiz += out_deg[i]
        for i in sg_allResNodes:
            if out_deg[i]>0:
                # page_rank[i] = out_deg[i]
                # if self.args.data_set == 'dblpname':
                # else:
                page_rank[i] = damping_value + damping_factor*out_deg[i]/sumRootOutNbSiz
        out_deg_dict = {node: deg for node, deg in DG.out_degree()}
    
        # pos = 0

        for _ in range(max_iterations):
            # pos = pos + 1
            old_pagerank = page_rank.copy()
            change = 0.0
            for i in can_pos_nodes:
                rank = damping_value
                rank += damping_factor * sum(old_pagerank[j] / out_deg_dict[j] for j in DG.predecessors(i))
                # for j in DG.predecessors(i):
                    # rank = rank + damping_factor * page_rank[j] / out_deg[j]
                change += abs(old_pagerank[i] - rank)
                page_rank[i] = rank
                # page_rank[i] = round(rank, 6)
            if change < min_delta:
                break
        
        
        for i in sg_allResNodes:
            page_rank[i] = 1.0
            
        if tag == 0:
            if self.args.data_set == 'dblpname':
                page_rank = [1.0 / i for i in page_rank]
            else:
                page_rank = [-1 * i for i in page_rank]

        '''Vertex Cover part'''
        cover_dict = {}
        cover_attr_dict = {}
        covered_set = set()
        covered_attr = set()
        attr_set = set()
        ppr_gain = []
        noattr_gain = []
        heapify(ppr_gain)
        heapify(noattr_gain)
        for i in can_pos_nodes:
            rank = page_rank[i]
            cover_set = {i}
            if self.args.data_set == 'dblpname':
               cover_attr = {self.upgraph.keywords[rmapper[i]].split("&",1)[0]}
            for nb in subgraph.neighbors(rmapper[i]):
                sg_nb = mapper[nb]
                rank = rank + page_rank[sg_nb]
                cover_set.add(sg_nb)
                if self.args.data_set == 'dblpname':
                    cover_attr.add(self.upgraph.keywords[nb].split("&",1)[0])
            if self.args.data_set != 'dblpname':
                heappush(ppr_gain, (-1 * rank, 0, i))
                cover_dict[i] = cover_set
            else:
                rank = (rank * (len(cover_attr)))/(rank+(len(cover_attr)))
                heappush(ppr_gain, (-1 * rank, 0, i))
                cover_dict[i] = cover_set
                cover_attr_dict[i] = cover_attr

        rec_num = 0
        rec_budget = 10
        rec_pos_nodes = []
        while rec_num < rec_budget and len(ppr_gain) > 0:
            vg = heappop(ppr_gain)
            if (vg[1] < rec_num):
                cover_set = cover_dict[vg[2]]
                remove_set = cover_set & covered_set
                rank = -1 * vg[0]
                if len(remove_set) > 0:
                    new_cover_set = cover_set - remove_set
                    cover_dict[vg[2]] = new_cover_set
                    for v in remove_set:
                        rank = rank - page_rank[v]
                if self.args.data_set != 'dblpname':
                    heappush(ppr_gain, (-1 * rank, rec_num, vg[2]))
                else:
                    cover_attr = cover_attr_dict[vg[2]]
                    remove_attr = cover_attr & covered_attr
                    new_cover_attr = set()
                    if len(remove_attr) > 0:
                        new_cover_attr = cover_attr - remove_attr
                        cover_attr_dict[vg[2]] = new_cover_attr
                    newrank = (rank * (len(new_cover_attr)))/(rank+(len(new_cover_attr)))
                    if(len(new_cover_attr)>0):
                        heappush(ppr_gain, (-1 * newrank, rec_num, vg[2]))
                    else:
                        # newrank = (rank * 0.9)/(rank+0.9)
                        # heappush(ppr_gain, (-1 * newrank, rec_num, vg[2]))
                        heappush(noattr_gain, (-1 * rank, 0, vg[2]))
            else:
                rec_num = rec_num + 1
                rec_pos_nodes.append(rmapper[vg[2]])
                covered_set_cp = covered_set
                covered_set = covered_set_cp.union(cover_dict[vg[2]])
            if self.args.data_set == 'dblpname':
                covered_attr_cp = covered_attr
                covered_attr = covered_attr_cp.union(cover_attr_dict[vg[2]])
        
        if self.args.data_set == 'dblpname':
            while rec_num < rec_budget and len(noattr_gain) > 0:
                vg = heappop(noattr_gain)
                if (vg[1] < rec_num):
                    cover_set = cover_dict[vg[2]]
                    remove_set = cover_set & covered_set
                    rank = -1 * vg[0]
                    if len(remove_set) > 0:
                        new_cover_set = cover_set - remove_set
                        cover_dict[vg[2]] = new_cover_set
                        for v in remove_set:
                            rank = rank - page_rank[v]
                    heappush(noattr_gain, (-1 * rank, rec_num, vg[2]))
                else:
                    rec_num = rec_num + 1
                    rec_pos_nodes.append(rmapper[vg[2]])
                    covered_set_cp = covered_set
                    covered_set = covered_set_cp.union(cover_dict[vg[2]])
                
        rec_time = time.time() - begin_time
        self.rec_res.append(rec_time)
        
        return rec_pos_nodes
    def random_recommend(self, seed, round):
        '''
        Random recommend posnodes and negnodes
        '''
        can_neg_nodes = []
        can_pos_nodes = []
        begin_time = time.time()
        for node in self.upgraph.allnode:
            if node not in self.upgraph.oldpos and node not in self.upgraph.oldneg:
                can_pos_nodes.append(node)
        random.shuffle(can_pos_nodes)
        rec_pos_nodes = can_pos_nodes[:10]
        rec_time = time.time() - begin_time
        self.rec_res.append(rec_time)

        begin_time = time.time()
        for node in self.upgraph.allnode:
            if node in self.upgraph.oldres and node not in self.upgraph.oldneg and node not in self.upgraph.oldpos:
                can_neg_nodes.append(node)
        random.shuffle(can_neg_nodes)
        rec_neg_nodes = can_neg_nodes[:10]
        rec_time = time.time() - begin_time
        self.rec_res.append(rec_time)
        self.upgraph.recnodes[round-1] = {0 : rec_pos_nodes, 1 : rec_neg_nodes}
        if self.args.data_set == 'dblpname':
            isok = self.userinter(seed, round, rec_pos_nodes, rec_neg_nodes)
        else:
            isok = self.evaluate_recommend(seed, round, rec_pos_nodes, rec_neg_nodes)
        return isok

    def ppr_recommend(self, seed, round):
        not_insert_nodes = set(self.upgraph.oldpos[:]) - set(self.upgraph.oldres[:])
        # print("not insert pos nodes")
        # print(not_insert_nodes)
        not_remove_nodes = set(self.upgraph.oldneg[:]) & set(self.upgraph.oldres[:])
        # print("not remove neg nodes")
        # print(not_remove_nodes)
        rec_pos_nodes = self.ppr_algo(1, self.upgraph.oldres[:] + list(not_insert_nodes), self.upgraph.allnode[:])
        # rec_pos_nodes = self.ppr_algo(1, self.upgraph.oldpos[:], self.upgraph.allnode[:])
        rec_neg_nodes = self.ppr_algo(0, self.upgraph.oldpos[:], list(set(self.upgraph.oldres[:]) - not_remove_nodes) + list(not_insert_nodes))
        self.upgraph.recnodes[round-1] = {0 : rec_pos_nodes, 1 : rec_neg_nodes}
        if self.args.data_set == 'dblpname':
            isok = self.userinter(seed, round, rec_pos_nodes, rec_neg_nodes)
        else:
            isok = self.evaluate_recommend(seed, round, rec_pos_nodes, rec_neg_nodes)
        
        return isok