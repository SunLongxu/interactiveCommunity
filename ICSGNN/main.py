from src.parser import parameter_parser
from src.subgraph import SubGraph
from src import utils
import torch
from torch_geometric.datasets import Reddit
from src.pre_community import *

def inittrain(args, seed, seedLabel, graph, target):
    posNodes = []
    negNodes = []
    allNodes = []
    length = args.subgraph_size   
    allNodes.append(seed)
    numLabel = int(length * args.train_ratio / 2)
    pos = 0
    while pos < len(allNodes) and pos < length and len(allNodes) < length:
        cnode = allNodes[pos]
        for nb in graph.neighbors(cnode):
            if nb not in allNodes and len(allNodes) < length:
                allNodes.append(nb)
        pos = pos + 1
    for node in allNodes:
        if node == seed or target[node] == seedLabel:
            posNodes.append(node)
        elif target[node] != seedLabel:
            negNodes.append(node)
    random.shuffle(posNodes)
    random.shuffle(negNodes)
    posNodes = posNodes[:numLabel]
    negNodes = negNodes[:numLabel]
    if (len(posNodes) < numLabel):
        print('e1')
        return 0, 0
    if (seed not in posNodes):
        posNodes[0] = seed
    if (len(negNodes) < numLabel):
        print('e2')
        return 0, 0
    return posNodes, negNodes   

def load_data(args):
    '''
    Load data
    '''
    seed_list = None
    train_nodes = None
    labels= None
    authorname=None
    keywords=None
    mapper=None
    if args.data_set in ['cora','citeseer','pubmed']:
        graph = utils.graph_reader1(args.data_set)
        features = utils.feature_reader1(args.data_set)
        target = utils.target_reader1(args.data_set)
    if args.data_set in ["reddit"]:
        path = osp.join(osp.dirname(osp.realpath(__file__)), 'data', args.data_set)
        data = Reddit(path)[0]
        edge_index, features, target = data.edge_index.numpy(), data.x.numpy(), data.y.numpy()
        graph = utils.construct_graph(edge_index)
        target = target[:, np.newaxis]
    if args.data_set in ["dblp", 'amazon', 'youtube', 'lj']:
        if args.iteration:
            edge, labels = load_snap(data_set='com_' + args.data_set, com_size=args.community_size)
            graph = nx.from_edgelist(edge)
            n = graph.number_of_nodes()
            features = np.array([np.random.normal(0, 1, 100) for _ in range(n)])
            target = None
        else:
            edge,seed_list,train_nodes,labels=pre_com(data_set='com_'+args.data_set,subgraph_list=[args.subgraph_size],train_ratio=args.train_ratio,cmty_size=args.community_size,seed_cnt=args.seed_cnt)
            graph = nx.from_edgelist(edge)
            n = graph.number_of_nodes()
            features=np.array([np.random.normal(0, 1, 100) for _ in range(n)])
            target=None
    if args.data_set in ["dblpname"]:
        if args.iteration:
            edge,feature,authorname,keywords,mapper = load_dblpname(data_set='com_' + args.data_set)
            graph = nx.from_edgelist(edge)
            n = graph.number_of_nodes()
            features = np.array(feature)
            # features = np.array([np.random.normal(0, 1, 100) for _ in range(n)])
            target = None
            labels = None

    return graph,features,target,seed_list,train_nodes,labels,authorname,keywords,mapper

def main():
    '''
    Parsing command line parameters
    '''
    args = parameter_parser()
    torch.manual_seed(args.seed)
    random.seed(args.seed)
    if(args.iteration==True):
        if args.data_set == 'dblpname':
            run_userstudy(args)
        else:
            run_iteration(args)
        return
    if args.data_set == 'facebook':
        run_facebook(args)
    else:
        run(args)
def run_facebook(args):
    '''
    Run the search at random community on each of Facebook's Ego Graphs
    In order to keep consistent with the paper experiment,
    the subgraph-size and train-ratio on Facebook are fixed as ego-graphs' size
    and 8/ ego-graphs' size. And there is no Rking loss in the paper experiment.
    '''
    seedlist = [0, 107, 348, 414, 686, 698, 1684, 1912, 3437, 3980]
    for seed in seedlist:
        args.ego = 'facebook_' + str(seed)
        edges, features, circle, seed = load_facebook(seed=seed)
        features = np.array(features)
        graph = nx.from_edgelist(edges)
        target = None
        args.subgraph_size=len(graph.nodes)
        args.train_ratio=8/args.subgraph_size
        subg = SubGraph(args, graph, features, target)
        utils.tab_printer(args)
        itr = 0
        while itr< args.seed_cnt:
            print("%d "% (itr)+20*'*')
            print("\nCurrent Seed Node is %d" % seed)
            label=circle[random.randint(0, len(circle) - 1)]
            random.shuffle(label)
            negnode=list(set(graph.nodes).difference(set(label+[seed])))
            random.shuffle(negnode)
            numlabel=int(args.subgraph_size*args.train_ratio/2)
            trian_node=label[:numlabel-1]+[seed]+negnode[:numlabel]
            print(trian_node,label,seed)
            isOK= subg.community_search(seed,trian_node,label+[seed])
            itr+=isOK
        subg.methods_result()

def run(args):
    '''
    Randomly selected seeds run the community search
    The SNAP dataset is pre-processed,Randomly select a community in which the node label 1 and the other node label 0
    '''
    graph, features, target, seed_list, train_nodes, labels, authorname, keywords, mapper = load_data(args)
    subg = SubGraph(args, graph, features, target)
    utils.tab_printer(args)
    trian_node = None
    label = None
    itr = 0
    while itr < args.seed_cnt:
        seed = random.randint(0, len(subg.graph.nodes) - 1)
        if (seed_list is not None):
            seed = seed_list[itr]
            trian_node = train_nodes[itr]
            label = labels[itr]
        print("%d " % (itr) + 20 * '*')
        print("\nCurrent Seed Node is %d" % seed)
        isOK = subg.community_search(seed, trian_node, label)
        itr+=isOK
    subg.methods_result()

def run_userstudy(args):
    '''
    Run community search with iteration for user study
    '''
    graph, features, target, _ ,_ ,com_list,authorname, keywords, mapper= load_data(args)
    utils.tab_printer(args)
    subg = SubGraph(args, graph, features, target, authorname, keywords)
    itr = 0
    '''
    #Lei Chen 0031# #875100#, #Yafei Li# #945270#, #Jianliang Xu# #27543#, #Christian S. Jensen# #12190#:
    Direction-Aware Why-Not Spatial Keyword Top-k Queries.

    #Jiaxin Jiang# #216327#, #Peipei Yi# #1179676#, #Byron Choi# #107248#, #Zhiwei Zhang# #74374#, #Xiaohui Yu# #16613#:
    Privacy-Preserving Reachability Query Services for Massive Networks.

    #Xin Huang 0001# #27802#, #Hong Cheng# #16617#, #Rong-Hua Li# #27801#, #Lu Qin# #15758#,#Jeffrey Xu Yu# #15757#:
    Top-K structural diversity search in large networks

    Yizhou Sun, Jiawei Han, Xifeng Yan, Philip S. Yu, Tianyi Wu:
    PathSim: Meta Path-Based Top-K Similarity Search in Heterogeneous Information Networks.

    Jian Pei, Bin Jiang, Xuemin Lin, Yidong Yuan:
    Probabilistic Skylines on Uncertain Data.

    Dawei Jiang, Beng Chin Ooi, Lei Shi, Sai Wu
    The Performance of MapReduce: An In-depth Study
    
    Hosagrahar V Jagadish, Beng Chin Ooi, Kian-Lee Tan, Cui Yu, Rui Zhang
    iDistance: An adaptive B+-tree based indexing method for nearest neighbor search

    #Lijun Chang# #484334#, #Xuemin Lin# #15721#, #Wenjie Zhang# #15731#, #Jeffrey Xu Yu# #15757#, Ying Zhang, Lu Qin:
    Optimal Enumeration: Efficient Top-k Tree Matching
    
    Jessica Lin, Eamonn J. Keogh, Stefano Lonardi, Bill Yuan-chi Chiu:
    A symbolic representation of time series, with implications for streaming algorithms

    #Geoffrey E. Hinton# #36326#, #Oriol Vinyals# #41768#, #Jeffrey Dean# #219413#:
    Distilling the Knowledge in a Neural Network

    #Jiawei Han 0001# #10296#, #Micheline Kamber# #139855#, #Jian Pei# #15416#:
    Data Mining: Concepts and Techniques

    #Xin Yao 0001# #34456# #evolutionary;learning;algorithms;optimization;algorithm;problem;using;dynamic;neural;problems;#
    #Zhenyu Yang# #102709# #multi;control;using;system;network;optimization;networks;#
    #Ke Tang# #241100# #optimization;algorithm;evolutionary;learning;algorithms;problems;multi;approach;evolution;#
    
    6. Xin Yao, Zhenyu Yang, Ke Tang: \n Large scale evolutionary optimization using cooperative coevolution \n
    New data
    48104,Jiawei Han 136292,Micheline Kamber 65115,Jian Pei
    85695,Matei A. Zaharia, 449156,Mosharaf Chowdhury,  66776,Scott Shenker, 37198,Ion Stoica
    441724,Reynold S. Xin
    DB: 920,Surajit Chaudhuri 64655,Michael Stonebraker
    44536,Joseph M. Hellerstein
    26726,Tim Kraska
    72715,Johannes Gehrke
    37198,Ion Stoica
    26726,Tim Kraska
    '''
    sugpaper = "Suggested query papers:\n 1. Lei Chen, Yafei Li, [Jianliang Xu], Christian S. Jensen: \n Direction-Aware Why-Not Spatial Keyword Top-k Queries. \n 2. Jiaxin Jiang, Peipei Yi, [Byron Choi], Zhiwei Zhang, Xiaohui Yu: \n Privacy-Preserving Reachability Query Services for Massive Networks.\n 3. [Xin Huang], Hong Cheng, Rong-Hua Li, Lu Qin, Jeffrey Xu Yu: \n Top-K structural diversity search in large networks \n 4. Geoffrey E. Hinton, Oriol Vinyals, Jeffrey Dean: Distilling the Knowledge in a Neural Network \n5. Jiawei Han, Micheline Kamber, Jian Pei: \n Data Mining: Concepts and Techniques. \n6. Xin Yao, Zhenyu Yang, Ke Tang: \n Large scale evolutionary optimization using cooperative coevolution \n"
    while itr < args.seed_cnt:
        # seed_list = [mapper[27543], mapper[107248], mapper[27802], mapper[36326], mapper[10296], mapper[34456]]
        seed_list = [mapper[48104], mapper[48014], mapper[48014]]
        # com_len_list = [1, 1, 1, 1, 1, 1]
        com_len_list = [1,1,1]
        target_list = []
        # posNodes_list = [[mapper[875100], mapper[945270], mapper[27543], mapper[12190]], [mapper[216327], mapper[1179676], mapper[107248], mapper[74374], mapper[16613]], [mapper[27802], mapper[16617], mapper[27801], mapper[15758], mapper[15757]], [mapper[36326], mapper[41768], mapper[219413]], [mapper[10296], mapper[139855], mapper[15416]], [mapper[34456], mapper[102709], mapper[241100]]]
        # negNodes_list = [[], [], [], [], [], []]
        posNodes_list = [[mapper[48104], mapper[136292], mapper[65115]], [mapper[48014], mapper[26726]], [mapper[48014], mapper[85695], mapper[449156], mapper[66776], mapper[37198]]]
        negNodes_list = [[], [], []]
        
        for i in range(len(seed_list)):
            tg =[ 1 if t in posNodes_list[i] else 0 for t in range(len(graph.nodes))]
            target_list.append(np.array(tg)[:, np.newaxis])
        print(sugpaper)
        q = int(input("Enter a number to select the query paper:"))-1
        if q < 0:
            subg.user_study_eva(itr)
            break
        subg.target = target_list[q]
        isOK = subg.community_search_iteration(seed_list[q], com_len_list[q], posNodes_list[q], negNodes_list[q], itr)
        itr = itr + isOK
    
    print("Search End. Thank you for your participant.")

def run_iteration(args):
    '''
    Run community search with iteration
    '''
    graph, features, target, _ ,_ ,com_list, authorname, keywords, _ = load_data(args)
    utils.tab_printer(args)
    subg = SubGraph(args, graph, features, target, authorname, keywords)
    itr = 0
    seed_list = []
    com_len_list = []
    target_list = []
    posNodes_list = []
    negNodes_list = []
    while itr < args.seed_cnt:
        if args.data_set in ['dblp','amazon', 'youtube', 'lj']:
                random.shuffle(com_list)
                com_max = com_list[0]
                com_len = len(com_max)
                target =[ 1 if i in com_max else 0 for i in range(len(graph.nodes))]
                seed = com_max[random.randint(0, len(com_max) - 1)]
                posNodes, negNodes = inittrain(args, seed, target[seed], graph, target)
        else:
                seed = random.randint(0, len(graph.nodes) - 1)
                com_max = [seed]
                com_len = 1
                target =[ 1 if i in com_max else 0 for i in range(len(graph.nodes))]
                posNodes, negNodes = inittrain(args, seed, target[seed], graph, target)
        if posNodes == 0:
            print("000")
            continue
        print("%d " % (itr) + 20 * '*')
        print("\nCurrent Seed Node is %d" % seed)
        target_list.append(np.array(target)[:, np.newaxis])
        seed_list.append(seed)
        com_len_list.append(com_len)
        posNodes_list.append(posNodes)
        negNodes_list.append(negNodes)
        itr += 1
    itr = 0
    while itr < len(seed_list):
        subg.target = target_list[itr]
        isOK = subg.community_search_iteration(seed_list[itr], com_len_list[itr], posNodes_list[itr], negNodes_list[itr], itr)
        itr += isOK
    subg.methods_result()

if __name__ == "__main__":	
    main()
