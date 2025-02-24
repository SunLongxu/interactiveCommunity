import numpy as np
import pandas as pd
import networkx as nx
from texttable import Texttable
from scipy.sparse import coo_matrix
import sys
import pickle as pkl
import scipy.sparse as sp
from networkx.readwrite import json_graph
import json
import os
import time
import datetime

def log(message):
    now = datetime.datetime.now()
    sTime = now.strftime("%Y_%m_%d_%H%M")
    with open('./log/log_'+sTime+'00.txt', 'a') as file:
        file.write(message + '\n')

def printlog(message):
    print(message)
    now = datetime.datetime.now()
    sTime = now.strftime("%Y_%m_%d_%H%M")
    with open('./log/log_'+sTime+'00.txt', 'a') as file:
        file.write(message + '\n')

def tab_printer(args):
    """
    Function to print the logs in a nice tabular format.
    :param args: Parameters used for the model.
    """
    args = vars(args)
    keys = sorted(args.keys())
    t = Texttable() 
    t.add_rows([["Parameter", "Value"]] +  [[k.replace("_"," ").capitalize(),args[k]] for k in keys])
    print(t.draw())

def construct_graph(edge_index):
    #graph = nx.Graph()
    #for i in range(edge_index.shape[-1]):
    #    graph.add_edge(edge_index[0][i], edge_index[1][i])
    edge_index=edge_index.swapaxes(0,1)
    graph = nx.from_edgelist(edge_index.tolist())
    return graph

def graph_reader(path):
    """
    Function to read the graph from the path.
    :param path: Path to the edge list.
    :return graph: NetworkX object returned.
    """
    graph = nx.from_edgelist(pd.read_csv(path).values.tolist())
    return graph

def feature_reader(path):
    """
    Reading the sparse feature matrix stored as csv from the disk.
    :param path: Path to the csv file.
    :return features: Dense matrix of features.
    """
    features = pd.read_csv(path)
    node_index = features["node_id"].values.tolist()
    feature_index = features["feature_id"].values.tolist()
    feature_values = features["value"].values.tolist()
    node_count = max(node_index)+1
    feature_count = max(feature_index)+1
    features = coo_matrix((feature_values, (node_index, feature_index)), shape=(node_count, feature_count)).toarray()
    return features

def target_reader(path):
    """
    Reading the target vector from disk.
    :param path: Path to the target.
    :return target: Target vector.
    """
    target = np.array(pd.read_csv(path)["target"]).reshape(-1,1)
    return target


def load_data(dataset_str):
    """
    Loads input data from gcn/data directory

    ind.dataset_str.x => the feature vectors of the training instances as scipy.sparse.csr.csr_matrix object;
    ind.dataset_str.tx => the feature vectors of the test instances as scipy.sparse.csr.csr_matrix object;
    ind.dataset_str.allx => the feature vectors of both labeled and unlabeled training instances
        (a superset of ind.dataset_str.x) as scipy.sparse.csr.csr_matrix object;
    ind.dataset_str.y => the one-hot labels of the labeled training instances as numpy.ndarray object;
    ind.dataset_str.ty => the one-hot labels of the test instances as numpy.ndarray object;
    ind.dataset_str.ally => the labels for instances in ind.dataset_str.allx as numpy.ndarray object;
    ind.dataset_str.graph => a dict in the format {index: [index_of_neighbor_nodes]} as collections.defaultdict
        object;
    ind.dataset_str.test.index => the indices of test instances in graph, for the inductive setting as list object.

    All objects above must be saved using python pickle module.

    :param dataset_str: Dataset name
    :return: All data input files loaded (as well the training/test data).
    """
    names = ['x', 'y', 'tx', 'ty', 'allx', 'ally', 'graph']
    objects = []
    for i in range(len(names)):
        with open("data/ind.{}.{}".format(dataset_str, names[i]), 'rb') as f:
            if sys.version_info > (3, 0):
                objects.append(pkl.load(f, encoding='latin1'))
            else:
                objects.append(pkl.load(f))

    x, y, tx, ty, allx, ally, graph = tuple(objects)
    test_idx_reorder = parse_index_file("data/ind.{}.test.index".format(dataset_str))
    test_idx_range = np.sort(test_idx_reorder)

    if dataset_str == 'citeseer':
        # Fix citeseer dataset (there are some isolated nodes in the graph)
        # Find isolated nodes, add them as zero-vecs into the right position
        test_idx_range_full = range(min(test_idx_reorder), max(test_idx_reorder)+1)
        tx_extended = sp.lil_matrix((len(test_idx_range_full), x.shape[1]))
        tx_extended[test_idx_range-min(test_idx_range), :] = tx
        tx = tx_extended
        ty_extended = np.zeros((len(test_idx_range_full), y.shape[1]))
        ty_extended[test_idx_range-min(test_idx_range), :] = ty
        ty = ty_extended

    features = sp.vstack((allx, tx)).tolil()
    features[test_idx_reorder, :] = features[test_idx_range, :]
    adj = nx.adjacency_matrix(nx.from_dict_of_lists(graph))

    labels = np.vstack((ally, ty))
    labels[test_idx_reorder, :] = labels[test_idx_range, :]

    idx_test = test_idx_range.tolist()
    idx_train = range(len(y))
    idx_val = range(len(y), len(y)+500)

    y_train = np.zeros(labels.shape)
    y_val = np.zeros(labels.shape)
    y_test = np.zeros(labels.shape)



def parse_index_file(filename):
    """Parse index file."""
    index = []
    for line in open(filename):
        index.append(int(line.strip()))
    return index


def graph_reader1(dataset_str):

    names = ['graph']
    objects = []
    for i in range(len(names)):
        with open("./data/{0}/ind.{0}.{1}".format(dataset_str, names[i]), 'rb') as f:
            if sys.version_info > (3, 0):
                objects.append(pkl.load(f, encoding='latin1'))
            else:
                objects.append(pkl.load(f))

    #graph = tuple(objects)
    adj = nx.from_dict_of_lists(objects[0])
    return adj


def feature_reader1(dataset_str, compression=0):

    names = ['x',  'tx',  'allx']
    objects = []
    for i in range(len(names)):
        with open("./data/{0}/ind.{0}.{1}".format(dataset_str, names[i]), 'rb') as f:
            if sys.version_info > (3, 0):
                objects.append(pkl.load(f, encoding='latin1'))
            else:
                objects.append(pkl.load(f))

    x, tx, allx  = tuple(objects)
    test_idx_reorder = parse_index_file("./data/{0}/ind.{0}.test.index".format(dataset_str))
    test_idx_range = np.sort(test_idx_reorder)

    if dataset_str == 'citeseer':
        # Fix citeseer dataset (there are some isolated nodes in the graph)
        # Find isolated nodes, add them as zero-vecs into the right position
        test_idx_range_full = range(min(test_idx_reorder), max(test_idx_reorder)+1)
        tx_extended = sp.lil_matrix((len(test_idx_range_full), x.shape[1]))
        tx_extended[test_idx_range-min(test_idx_range), :] = tx
        tx = tx_extended

    features = sp.vstack((allx, tx)).tolil()
    features[test_idx_reorder, :] = features[test_idx_range, :]
    preprocess_features(features)
    features = features.tocoo()
    features = features.toarray()

    feature_list = []
    feature_list.append(features)



    #features = sp.vstack((allx, tx)).tocoo()
    """
    features = sp.vstack((allx, tx)).tocoo()
    preprocess_features(features)
    features = features.toarray()
    """
    #features[test_idx_reorder, :] = features[test_idx_range, :]


    #features = coo_matrix((feature_values, (node_index, feature_index)), shape=(node_count, feature_count)).toarray()

    return features

def preprocess_features(features):
    """Row-normalize feature matrix and convert to tuple representation"""
    rowsum = np.array(features.sum(1))
    r_inv = np.power(rowsum, -1).flatten()
    r_inv[np.isinf(r_inv)] = 0.
    r_mat_inv = sp.diags(r_inv)
    features = r_mat_inv.dot(features)
    return features

def target_reader1(dataset_str):
    """
    Loads input data from gcn/data directory

    ind.dataset_str.x => the feature vectors of the training instances as scipy.sparse.csr.csr_matrix object;
    ind.dataset_str.tx => the feature vectors of the test instances as scipy.sparse.csr.csr_matrix object;
    ind.dataset_str.allx => the feature vectors of both labeled and unlabeled training instances
        (a superset of ind.dataset_str.x) as scipy.sparse.csr.csr_matrix object;
    ind.dataset_str.y => the one-hot labels of the labeled training instances as numpy.ndarray object;
    ind.dataset_str.ty => the one-hot labels of the test instances as numpy.ndarray object;
    ind.dataset_str.ally => the labels for instances in ind.dataset_str.allx as numpy.ndarray object;
    ind.dataset_str.graph => a dict in the format {index: [index_of_neighbor_nodes]} as collections.defaultdict
        object;
    ind.dataset_str.test.index => the indices of test instances in graph, for the inductive setting as list object.

    All objects above must be saved using python pickle module.

    :param dataset_str: Dataset name
    :return: All data input files loaded (as well the training/test data).
    """

    names = [ 'y',  'ty',  'ally']
    objects = []
    for i in range(len(names)):
        with open("./data/{0}/ind.{0}.{1}".format(dataset_str, names[i]), 'rb') as f:
            if sys.version_info > (3, 0):
                objects.append(pkl.load(f, encoding='latin1'))
            else:
                objects.append(pkl.load(f))

    y, ty,  ally = tuple(objects)
    test_idx_reorder = parse_index_file("./data/{0}/ind.{0}.test.index".format(dataset_str))
    test_idx_range = np.sort(test_idx_reorder)

    #target = np.array(pd.read_csv(path)["target"]).reshape(-1, 1)

    if dataset_str == 'citeseer':
        # Fix citeseer dataset (there are some isolated nodes in the graph)
        # Find isolated nodes, add them as zero-vecs into the right position
        test_idx_range_full = range(min(test_idx_reorder), max(test_idx_reorder)+1)

        ty_extended = np.zeros((len(test_idx_range_full), y.shape[1]))
        ty_extended[test_idx_range-min(test_idx_range), :] = ty
        ty = ty_extended

    #labels = np.vstack((ally, ty)).reshape(-1,1)

    #labels = np.vstack((ally, ty))
    #labels = labels.reshape(-1, 1)
    #labels = np.append(ally, ty)

    ally = np.argmax(ally, axis=1)
    ty = np.argmax(ty, axis=1)
    labels = np.concatenate((ally, ty))
    labels[test_idx_reorder] = labels[test_idx_range]
    labels = labels.reshape(-1,1)


    """
    ally = np.argmax(ally, axis=1)
    ty = np.argmax(ty, axis=1)
    labels = np.concatenate((ally, ty), axis=0).reshape(-1,1)
    """
    return labels


def load_data(prefix, normalize=True, load_walks=False):
    G_data = json.load(open(prefix + "-G.json"))
    G = json_graph.node_link_graph(G_data)
    if isinstance(G.nodes()[0], int):
        conversion = lambda n: int(n)
    else:
        conversion = lambda n: n

    if os.path.exists(prefix + "-feats.npy"):
        feats = np.load(prefix + "-feats.npy")
    else:
        print("No features present.. Only identity features will be used.")
        feats = None
    id_map = json.load(open(prefix + "-id_map.json"))
    id_map = {conversion(k): int(v) for k, v in id_map.items()}


    walks = []
    class_map = json.load(open(prefix + "-class_map.json"))
    if isinstance(list(class_map.values())[0], list):
        lab_conversion = lambda n: n
    else:
        lab_conversion = lambda n: int(n)

    class_map = {conversion(k): lab_conversion(v) for k, v in class_map.items()}
    #class_map1 = {k: v.index(1) for k,v in class_map.items()}
    target =[0]*len(G.nodes)

    zero=0
    if "ppi" in prefix:
        for cnode in G.nodes():
            cid = id_map[str(cnode)]
            if 1 not in class_map[str(cnode)]:
                zero = zero+1
                target[cid] = -1
            else:
                classlabel = class_map[str(cnode)].index(1)
                target[cid] = classlabel
        print("The number of nodes with zero labels is %d" %zero)
        maxlabel = max(target) + 1

        target = [maxlabel + 1 if x == -1 else x for x in target]
        print("The maxiaml label is %d" % maxlabel)
    else:
        cnt=0
        for cnode in G.nodes():
            #print(cnode)
            if cnode ==0:
                print(cnt)
                continue
            cid = id_map[str(cnode)]
            cnt = cnt+1
            #print(cid)
            target[cid] = class_map[str(cnode)]


    target =  np.array(target).reshape(-1,1)
    ## Remove all nodes that do not have val/test annotations
    ## (necessary because of networkx weirdness with the Reddit data)
    """
    broken_count = 0
    brokens = []
    for node in G.nodes():
        if not 'val' in G.nodes[node] or not 'test' in G.nodes[node]:
            # G.remove_node(node)
            brokens.append(node)
            broken_count += 1
    for node in brokens:
        G.remove_node(node)

    print("Removed {:d} nodes that lacked proper annotations due to networkx versioning issues".format(broken_count))
    """

    ## Make sure the graph has edge train_removed annotations
    ## (some datasets might already have this..)
    print("Loaded data.. now preprocessing..")
    for edge in G.edges():
        if (G.nodes[edge[0]]['val'] or G.nodes[edge[1]]['val'] or
                G.nodes[edge[0]]['test'] or G.nodes[edge[1]]['test']):
            G[edge[0]][edge[1]]['train_removed'] = True
        else:
            G[edge[0]][edge[1]]['train_removed'] = False

    if normalize and not feats is None:
        from sklearn.preprocessing import StandardScaler
        train_ids = np.array([id_map[str(n)] for n in G.nodes()])
        feats = feats[train_ids]
        scaler = StandardScaler()
        scaler.fit(feats)
        feats = scaler.transform(feats)

    if load_walks:
        with open(prefix + "-walks.txt") as fp:
            for line in fp:
                walks.append(map(conversion, line.split()))

    #return G, feats, id_map, walks, class_map
    return G, feats, target

def graphtofile(graph, target):
    fo = open("graphedge.txt", "w")
    for edge in graph.edges:
        fo.write("%d, %d\n" %(edge[0], edge[1]))
    fo.close()

    fo = open("graphlabel.txt", "w")
    for i in range(len(target)):
        fo.write("%d, %d\n" %(i,target[i][0]))
    fo.close()

