import networkx as nx
from src import utils



class LocalCommunity(object):

    def __init__(self, args, upgraph):
        self.args = args
        self.upgraph = upgraph
    def f1score(self, true_cmty, res_cmty):
        true_set = set(true_cmty)
        res_set = set(res_cmty)
        intersection = true_set & res_set
        pr = len(intersection) / len(res_set)
        re = len(intersection) / len(true_set)
        f1 = (2*pr*re) / (pr + re)
        return pr, re, f1
    def compute_pre(self, true_cmty, res_cmty):
        true_set = set(true_cmty)
        res_set = set(res_cmty)
        intersection = true_set & res_set
        pr = len(intersection) / len(res_set)
        return pr
    def userstudy_evaluate(self, top_index, final_res, method_name, using_time, round):
        '''
        Evaluate the quality of the search community
        '''
        # final_res = top_index[self.args.round-1]
        for i in range(self.args.round):
            
            pre, rec, f1 = self.f1score(final_res, top_index[i])
            results= []
            results.append(round)
            results.append(pre)
            results.append(rec)
            results.append(f1)
            name = str(i)+' Round' + method_name
            if name not in self.upgraph.methods:
                self.upgraph.methods[name]= results
                self.upgraph.time_map[name] = using_time[i]
                self.upgraph.cntmap[name]=1
            else:
                self.upgraph.methods[name] =[self.upgraph.methods[name][i]+results[i] for i in range(len(results))]
                self.upgraph.time_map[name] += using_time[i]
                self.upgraph.cntmap[name]+=1
            utils.log(name + " Precision={:.4f} using {:.4f}s".format(results[1],using_time[i]))
            utils.log(name + " Recall={:.4f} ".format(results[2]))
            utils.log(name + " F1Score={:.4f} ".format(results[3]))
        for i in range(self.args.round-1):
            rec_res = []
            rec_res.append(self.compute_pre(final_res, self.upgraph.recnodes[i][0]))
            rec_res.append(1- self.compute_pre(final_res, self.upgraph.recnodes[i][1]))
            if i in self.upgraph.rec_evauser:
                self.upgraph.rec_evauser[i] = [self.upgraph.rec_evauser[i][j]+rec_res[j] for j in range(len(rec_res))]
            else:
                self.upgraph.rec_evauser[i] = rec_res
            utils.log(str(i+1) +"Round " + "Positive Nodes Recommend Precision={:.4f}".format(rec_res[0]))
            utils.log(str(i+1) +"Round " + "Negative Nodes Recommend Precision={:.4f}".format(rec_res[1]))
            
        return results[0]
    def evaluate_community(self, top_index, com_len,  name,using_time):
        '''
        Evaluate the quality of the search community
        '''
        y_pred = [0] * len(top_index)
        y_true = [0] * len(top_index)
        ok = 0
        pos = 0
        target = self.upgraph.sg_targets[0].cpu().detach().numpy().tolist()
        for i in range(len(top_index)):
            y_pred[i] = self.upgraph.sg_predLabels[top_index[i]]
            y_true[i] = target[top_index[i]][0]
            if top_index[i] in self.upgraph.sg_posNodes or top_index[i] in self.upgraph.posforrank:
                pos+=1
            if(y_true[i]==1):
                ok+=1
        results= []
        pre=ok/len(top_index)
        rec=ok/com_len
        f1=2 * (pre * rec) / (pre + rec)
        '''
        For iteration only
        '''
        if self.args.iteration == True:
        #     cmty_siz_now = self.upgraph.cmty_siz
        #     change_siz = int(cmty_siz_now * 0.05)
            if (f1 == 1.0):
                self.upgraph.round = self.args.round
        #     elif (pre == 1.0):
        #         self.upgraph.cmty_siz = cmty_siz_now + change_siz
        #     elif (rec == 1.0):
        #         self.upgraph.cmty_siz = cmty_siz_now - change_siz
        pre1=0
        rec1=0
        f11=0
        if (len(top_index) != pos and ok != pos):
            pre1=(ok-pos)/(len(top_index)-pos)
            rec1=(ok-pos)/(com_len-pos)
            f11=2 * (pre1 * rec1) / (pre1 + rec1)
        results.append(pre)
        results.append(pre1)
        results.append(rec)
        results.append(rec1)
        results.append(f1)
        results.append(f11)
        if name not in self.upgraph.methods:
            self.upgraph.methods[name]= results
            self.upgraph.time_map[name] =  using_time
            self.upgraph.cntmap[name]=1
        else:
            self.upgraph.methods[name] =[self.upgraph.methods[name][i]+results[i] for i in range(len(results))]
            self.upgraph.time_map[name] += using_time
            self.upgraph.cntmap[name]+=1
        print(name + " Precision={:.4f}  Precision without posnode={:.4f} using {:.4f}s".format(results[0],results[1],using_time))
        print(name + " Recall={:.4f}  Recall without posnode={:.4f}".format(results[2],results[3]))
        print(name + " F1Score={:.4f}  F1Score without posnode={:.4f}".format(results[4],results[5]))
        return results[0]





    def locate_community_BFS_only(self, seed):
        '''
        Search community using bfs only
        '''
        cnodes = []
        cnodes.append(seed)
        pos =0
        while pos < len(cnodes) and pos < self.upgraph.TOPK_SIZE and len(cnodes) < self.upgraph.TOPK_SIZE:
            cnode = cnodes[pos]
            for nb in self.upgraph.subgraph.neighbors(cnode):
                if nb not in cnodes and len(cnodes) < self.upgraph.TOPK_SIZE:
                    cnodes.append(nb)
            pos = pos + 1

        topk = [self.upgraph.mapper[node] for node in cnodes]
        return topk


    def locate_community_BFS(self, seed):
        '''
        Search community using bfs with swap
        '''
        cnodes = []
        cnodes.append(seed)
        pos =0
        while pos < len(cnodes) and pos < self.upgraph.TOPK_SIZE and len(cnodes) < self.upgraph.TOPK_SIZE:
            cnode = cnodes[pos]
            for nb in self.upgraph.subgraph.neighbors(cnode):
                if nb not in cnodes and len(cnodes) < self.upgraph.TOPK_SIZE:
                    cnodes.append(nb)
            pos = pos + 1
        for pos in range(len(cnodes)):
            cnode = cnodes[pos]
            for nb in self.upgraph.subgraph.neighbors(cnode):
                 pos1= pos+1
                 while pos1<len(cnodes) and nb not in cnodes:
                    next = cnodes[pos1]

                    if self.upgraph.sg_predProbs[self.upgraph.mapper[nb]]>self.upgraph.sg_predProbs[self.upgraph.mapper[next]]:
                        cnodes[pos1] = nb
                    pos1 = pos1 +1
        topk = [self.upgraph.mapper[node] for node in cnodes]
        return topk


    def locate_community_greedy(self, seed):
        '''
        Search community using  greedy-T
        '''
        cnodes = [ ]
        parents =[0] * len(self.upgraph.subgraph.nodes)
        cnodes.append(seed)
        parents[cnodes.index(seed)] =-1
        pos =0
        while pos < len(cnodes) :
            cnode = cnodes[pos]
            for nb in self.upgraph.subgraph.neighbors(cnode):
                if nb not in cnodes:
                    cnodes.append(nb)
                    parents[cnodes.index(nb)]=cnodes.index(cnode)
            pos = pos + 1
        topkidx=[]
        topkidx.append(0)
        for _ in range(self.upgraph.TOPK_SIZE):
            if(len(topkidx)==self.upgraph.TOPK_SIZE):break
            probs = [-1.0] * len(self.upgraph.subgraph.nodes)
            hops = [1] * len(self.upgraph.subgraph.nodes)
            for i in range(len(cnodes)):
                cnode = cnodes[i]
                if i in topkidx:
                    continue
                prob = self.upgraph.sg_predProbs[self.upgraph.mapper[cnode]]
                while parents[cnodes.index(cnode)] != -1:
                    cnode = cnodes[parents[cnodes.index(cnode)]]
                    if cnodes.index(cnode) in topkidx:
                        break
                    prob = prob + self.upgraph.sg_predProbs[self.upgraph.mapper[cnode]]
                    hops[i] = hops[i]+1
                probs[i] = prob
            for i in range(len(probs)):
                probs[i] = probs[i]/hops[i]
            maxValueIdx = probs.index(max(probs))
            if len(topkidx)<self.upgraph.TOPK_SIZE:
                topkidx.append(maxValueIdx)
            while parents[maxValueIdx] != -1:
                maxValueIdx = parents[maxValueIdx]
                if maxValueIdx not in topkidx and len(topkidx)<self.upgraph.TOPK_SIZE:
                    topkidx.append(maxValueIdx)
        topk =[self.upgraph.mapper[cnodes[idx]] for idx in topkidx]
        return topk


    def locate_community_greedy_graph_prepath(self, seed):
        '''
        Search community using  greedy-G
        '''
        cnodes = []
        cnodes.append(seed)
        pos = 0
        while pos < len(cnodes):
            cnode = cnodes[pos]
            for nb in self.upgraph.subgraph.neighbors(cnode):
                if nb not in cnodes:
                    cnodes.append(nb)
            pos = pos + 1
        topkidx = []
        topkidx.append(seed)
        for iter in range(self.upgraph.TOPK_SIZE):
            if (len(topkidx) == self.upgraph.TOPK_SIZE): break
            candidates = [-1.0] * 3 * self.upgraph.TOPK_SIZE
            for num in range(len(topkidx)):
                candidates[num] = 1.0
            probs = [-1.0] * len(self.upgraph.subgraph.nodes)
            hops = [1] * len(self.upgraph.subgraph.nodes)
            paths = self.get_all_path(self.upgraph.subgraph, topkidx,cnodes)
            for i in range(len(cnodes)):
                cnode = cnodes[i]
                if cnode in topkidx:
                    continue
                prob = self.upgraph.sg_predProbs[self.upgraph.mapper[cnode]]
                if prob < min(candidates):
                    continue
                for x in paths[i]:
                    prob = prob + self.upgraph.sg_predProbs[self.upgraph.mapper[x]]
                probs[i] = prob
                hops[i] = len(paths[i]) + 1
                prob = prob / hops[i]
                idx = candidates.index(min(candidates))
                candidates[idx] = prob
            for i in range(len(probs)):
                probs[i] = probs[i] / hops[i]
            maxValueIdx = probs.index(max(probs))
            if len(topkidx) < self.upgraph.TOPK_SIZE:
                topkidx.append(cnodes[maxValueIdx])
            for x in paths[maxValueIdx]:
                if len(topkidx) < self.upgraph.TOPK_SIZE:
                    topkidx.append(x)
        topk = [self.upgraph.mapper[idx] for idx in topkidx]
        return topk
    def get_all_path(self, pgraph, topkidx,cnodes):
        '''
        Get the community to the other nodes' shortest paths
        '''
        g = nx.Graph()
        seed = topkidx[0]
        for u, v in pgraph.edges:
            if (u in topkidx) and (v in topkidx):
                continue
            else:
                if (u in topkidx): u = seed
                if (v in topkidx): v = seed
                g.add_edge(u, v)
        p1 = nx.shortest_path(g, source=seed)
        paths = [[]] * len(pgraph.nodes)
        for item in p1.keys():
            idx=cnodes.index(item)
            path = p1[item][1:]
            path.reverse()
            paths[idx] = path[1:]
        return paths
