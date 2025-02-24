import networkx as nx
import numpy as np
import random
import os, sys
import gzip
import pathlib
import  tarfile
import collections
import re
from collections import Counter
p = os.path.dirname(os.path.dirname((os.path.abspath('__file__'))))
if p not in sys.path:
    sys.path.append(p)
import os.path as osp

def pre_com(data_set='com_dblp',subgraph_list=[400], train_ratio=0.02,seed_cnt=20,cmty_size=30):
    path = osp.join(osp.dirname(osp.realpath(__file__)), '..','data',data_set)
    print(f"Load {data_set} edges")
    if(os.path.exists(path + '//edge.npy') == False):
        untar_snap_data(data_set[4:])
    new_edge=np.load(path+'//edges.npy').tolist()
    graph = nx.from_edgelist(new_edge)
    print(f"Load {data_set} cmty")
    com_list=np.load(path+'//comms.npy',allow_pickle=True).tolist()

    com_len=[(i,len(line)) for i,line in enumerate(com_list)]
    com_len.sort(key=lambda x:x[1],reverse=True) 
    for subgraph_size in subgraph_list:
        numlabel = int(subgraph_size * train_ratio / 2) 
        ok_com_len=[(i,lens) for i,lens in com_len if lens>=(numlabel+cmty_size) ]
        seed_list=[]
        train_node=[]
        labels=[]
        error_seed=[]
        time=0
        while len(seed_list)<seed_cnt:
            time+=1
            seed_com_index=random.randint(0,len(ok_com_len)-1)
            seed_com=com_list[ok_com_len[seed_com_index][0]]
            seed_com_suff=seed_com[:]
            random.shuffle(seed_com_suff)
            seed_index=0
            seed=seed_com_suff[seed_index]
            while (seed in seed_list or seed in error_seed ) and (seed_index+1)<len(seed_com_suff):
                seed_index+=1
                seed=seed_com_suff[seed_index]
            if(seed in seed_list or seed in error_seed ): 
                continue
            allNodes=[] 
            allNodes.append(seed)
            pos = 0
            while pos < len(allNodes) and pos < subgraph_size and len(allNodes) < subgraph_size:
                cnode = allNodes[pos]
                for nb in graph.neighbors(cnode):
                    if nb not in allNodes and len(allNodes) < subgraph_size:
                        allNodes.append(nb)
                pos += 1
            posNodes = []
            posNodes.append(seed)
            seed_com_intersection=list(set(seed_com).intersection(set(allNodes)))
            if(len(seed_com_intersection)< numlabel+cmty_size):
                error_seed.append(seed)
                continue
            seed_com_intersection_noseed=seed_com_intersection[:]
            seed_com_intersection_noseed.remove(seed)
            random.shuffle(seed_com_intersection_noseed)
            posNodes.extend(seed_com_intersection_noseed[:numlabel-1])
            negNodes=list(set(allNodes).difference(set(seed_com)))
            if(len(negNodes)< numlabel):
                error_seed.append(seed)
                continue
            random.shuffle(negNodes)
            negNodes=negNodes[:numlabel]
            seed_list.append(seed)
            train_node.append(posNodes+negNodes)
            labels.append(seed_com_intersection)
        print('error:',len(error_seed),"seed_list:",seed_list)
    return new_edge,seed_list,train_node,labels

def load_facebook(seed):
    path = osp.join(osp.dirname(osp.realpath(__file__)), '..', 'data','facebook')
    print('Load facebook data')
    if(os.path.exists(path + f'//{str(seed)}.circles') == False):
        untart_facebook()
    file_circle= path + f'//{str(seed)}.circles'
    file_edges=path + f'//{str(seed)}.edges'
    file_egofeat=path + f'//{str(seed)}.egofeat'
    file_feat=path + f'//{str(seed)}.feat'
    edges=[]
    node=[]
    feature = {}
    with open(file_egofeat) as f:
        feature[seed] = [int(i) for i in f.readline().split()]
    with open(file_feat) as f:
        for line in f:
            line = [int(i) for i in line.split()]
            feature[int(line[0])] = line[1:]
            node.append(int(line[0]))
    with open(file_edges,'r') as f:
        for line in f:
            u,v=line.split()
            u=int(u)
            v=int(v)
            if(u in feature.keys() and v in feature.keys()):
                edges.append((u,v))

    for i in node:
        edges.append((seed, i))
    node=sorted(node+[seed])
    mapper = {n: i for i, n in enumerate(node)}
    edges=[(mapper[u],mapper[v]) for u,v in edges]
    node=[mapper[u] for u in node]

    features=[0]*len(node)
    for i in list(feature.keys()):
        features[mapper[i]]=feature[i]
    circle=[]
    with open(file_circle) as f:
        for line in f:
            line=line.split()
            line=[ mapper[int(i)] for i  in line[1:]]
            if(len(line)<8):continue
            circle.append(line)

    seed=mapper[seed]

    return edges,features,circle,seed

def load_snap(data_set,com_size):
    print(f'Load {data_set} edge')
    path = osp.join(osp.dirname(osp.realpath(__file__)), '..', 'data', data_set)
    if(os.path.exists(path + '//edges.npy') == False):
        untar_snap_data(data_set[4:])
    edges=np.load(path + '//edges.npy').tolist()
    print(f'Load {data_set} cmty')
    com_list = np.load(path + '//comms.npy', allow_pickle=True).tolist()
    com_list=[i for i in com_list if len(i)>=com_size]
    return edges,com_list

def untar_snap_data(name):
    """Load the snap comm datasets."""
    print(f'Untar {name} edge')
    root = pathlib.Path('raw')
    #print(root)
    with gzip.open(root / f'com-{name}.ungraph.txt.gz', 'rt') as fh:
        edges = fh.read().strip().split('\n')[4:]
    edges = [[int(i) for i in e.split()] for e in edges]
    nodes = {i for x in edges for i in x}
    mapping = {u: i for i, u in enumerate(sorted(nodes))}
    edges = [[mapping[u], mapping[v]] for u, v in edges]
    print(f'Untar {name} cmty')
    with gzip.open(root / f'com-{name}.top5000.cmty.txt.gz', 'rt') as fh:
        comms = fh.readlines()
    comms = [[mapping[int(i)] for i in x.split()] for x in comms]
    root = pathlib.Path()/'data'/f'com_{name}'
    root.mkdir(exist_ok=True, parents=True)
    np.save(root/'edges',edges)
    np.save(root/'comms',comms,allow_pickle=True)
    np.save(root/'map',mapping,allow_pickle=True)

def load_dblpname(data_set):
    print(f'Load {data_set} edge')
    path = osp.join(osp.dirname(osp.realpath(__file__)), '..', 'data', data_set)
    if(os.path.exists(path + '//edges.npy') == False):
        load_dblpnew_data(data_set)
        # load_dblpname_data(data_set)
    edges=np.load(path + '//edges.npy').tolist()
    features = np.load(path + '//features.npy',allow_pickle=True).tolist()
    auname = np.load(path + '//auname.npy',allow_pickle=True).tolist()
    keywords = np.load(path + '//keywords.npy',allow_pickle=True).tolist()
    mapper = np.load(path + '//mapper.npy',allow_pickle=True).tolist()
    
    print(f'Load {data_set} success')
    return edges,features,auname,keywords,mapper

def filter_dblpname_data(data_set):
    """Load the dblp name dataset."""
    path = osp.join(osp.dirname(osp.realpath(__file__)), '..', 'data', data_set)
    file_feat=path + f'//dblp-public-author-list'
    file_write=path + f'//dblp-public-author-list-filter'
    feature = {}
    alist = []
    keywords = set()
    lines=[]
    
    with open(file_feat) as f:
        for line in f:
            line = re.findall('#(.*?)#', line.rstrip("\n"))
            line = [x.strip() for x in line]
            lines.append(line)

    for line in reversed(lines):
        kw = [ele for ele in line[2].split(";") if len(ele) > 3]
        alist.extend(kw)
    countkw = Counter(alist)
    filtered = {k for k, v in countkw.items() if v > 6000}
    keywords = sorted(set(filtered))
    print(keywords)
    print(len(keywords))
    for line in reversed(lines):
        feature[int(line[1])] = [ele for ele in line[2].split(";") if ele in keywords]  
    with open(file_write, 'w') as f:
        for line in lines:
            if len(feature[int(line[1])])>0:
                s = f"#{line[0]}# #{line[1]}# #{';'.join(str(feat) for feat in feature[int(line[1])])};#\n"
            else:
                s = f"#{line[0]}# #{line[1]}# ##\n"
            f.write(s)

def load_dblpname_data(data_set):
    """Load the dblp name dataset."""
    path = osp.join(osp.dirname(osp.realpath(__file__)), '..', 'data', data_set)
    file_edges=path + f'//dblp-public-graph'
    file_feat=path + f'//dblp-public-author-list-filter'
    edges=[]
    node=[]
    authorname = {}
    feature = {}
    alist = []
    keywords = set()
    lines=[]
    with open(file_feat) as f:
        for line in f:
            line = re.findall('#(.*?)#', line.rstrip("\n"))
            line = [x.strip() for x in line]
            lines.append(line)

    for line in reversed(lines):
        authorname[int(line[1])] = line[0]
        node.append(int(line[1]))
        kw = [ele for ele in line[2].split(";")]
        alist.extend(kw)
    keywords = sorted(set(alist))
    # print(keywords)
    # print(len(keywords))
    for line in reversed(lines):
        feature[int(line[1])] = [int(keyword in line[2].split(";")) for keyword in keywords]    
    with open(file_edges,'r') as f:
        for line in f:
            u,v=line.split()
            u=int(u.strip("#"))
            v=int(v.strip("#"))
            if(u in feature.keys() and v in feature.keys()):
                edges.append((u,v))
    node = {i for x in edges for i in x}
    
    mapper = {n: i for i, n in enumerate(node)}
    edges=[(mapper[u],mapper[v]) for u,v in edges]
    node=[mapper[u] for u in node]

    features=[0]*len(node)
    auname=[0]*len(node)
    for i in list(feature.keys()):
        if i in mapper.keys():
            features[mapper[i]]=feature[i]
            auname[mapper[i]]=authorname[i]

    root = pathlib.Path()/'data'/f'{data_set}'
    root.mkdir(exist_ok=True, parents=True)
    np.save(root/'edges',edges)
    np.save(root/'features',features,allow_pickle=True)
    np.save(root/'auname',auname,allow_pickle=True)
    np.save(root/'mapper',mapper,allow_pickle=True)

    # return edges,features,auname,mapper

def load_dblpnew_data(data_set):
    """Load the dblp new dataset."""
    path = osp.join(osp.dirname(osp.realpath(__file__)), '..', 'data', data_set)
    file_edges=path + f'//edges.txt'
    file_feat=path + f'//vertex_to_field.txt'
    file_author=path + f'//vertex_to_name.txt'
    edges=[]
    node=[]
    feature = {}
    authorkeyword = {}
    alist = set()

    name_id_map = {}
    id_name_map = {}
    id_map = {}

    # Read the file and process each line
    with open(file_author, 'r') as file:
        for line in file:
            parts = line.strip().split(',')
            current_id = int(parts[0])
            current_name = parts[1].strip()

            # Check if the name already exists in the map
            if current_name in name_id_map:
                new_id = name_id_map[current_name]
            else:
                new_id = current_id

            # Update the map with the new ID for the name
            name_id_map[current_name] = new_id
            id_name_map[new_id] = current_name
            id_map[current_id] = new_id

    with open(file_edges,'r') as f:
        edges = f.read().strip().split('\n')
    edges = [[int(i) for i in e.split()] for e in edges]
    edges=[(id_map[u],id_map[v]) for u,v in edges]
    unique_edges = set()

    for edge in edges:
        sorted_edge = tuple(sorted(edge))
        unique_edges.add(sorted_edge)
    unique_edges_list = list(unique_edges)

    node = {i for x in unique_edges_list for i in x}
    mapper = {n: i for i, n in enumerate(node)}
    edges=[(mapper[u],mapper[v]) for u,v in unique_edges_list]
    node=[mapper[u] for u in node]
    features=[0]*len(node)
    authorname=[0]*len(node)
    authorkeywords=[0]*len(node)
    featurelist=[0]*len(node)
    id_label_dict = {}

    with open(file_feat, 'r') as file:
        for line in file:
            parts = line.strip().split(',')
            current_id = id_map[int(parts[0])]
            if current_id in mapper.keys():
                current_label = parts[1].strip()
                alist.add(current_label)

                # Check if the id already exists in the dictionary
                if current_id in id_label_dict:
                    id_label_dict[current_id].add(current_label)
                else:
                    id_label_dict[current_id] = {current_label}

    keywords = sorted(alist)
    combined_list = [(id_val, list(labels)) for id_val, labels in id_label_dict.items()]
    
    for id_val, labels in combined_list:
        feature[id_val] = [int(keyword in labels) for keyword in keywords]  
        authorkeyword[id_val] = '&'.join(labels)
    for i in list(feature.keys()):
        if i in mapper.keys():
            features[mapper[i]]=feature[i]
            authorkeywords[mapper[i]]=authorkeyword[i]
    for id,name in id_name_map.items():
        if id in mapper.keys():
            authorname[mapper[id]] = name

    root = pathlib.Path()/'data'/f'{data_set}'
    root.mkdir(exist_ok=True, parents=True)
    np.save(root/'edges',edges)
    np.save(root/'features',features,allow_pickle=True)
    np.save(root/'auname',authorname,allow_pickle=True)
    np.save(root/'keywords',authorkeywords,allow_pickle=True)
    np.save(root/'mapper',mapper,allow_pickle=True)

def untart_facebook():
    print(f'Untar  facebook')
    tar = tarfile.open(osp.join(osp.dirname(osp.realpath(__file__)), '..', 'raw','facebook.tar.gz'))
    names = tar.getnames()
    path =osp.join(osp.dirname(osp.realpath(__file__)), '..', 'data')
    for name in names:
        tar.extract(name,path)
    tar.close()