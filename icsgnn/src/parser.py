import argparse

def parameter_parser():
   
    parser = argparse.ArgumentParser(description = "Run .")
    parser.add_argument("--epochs",
                        type = int,
                        default = 100,
	                    help = "Number of training epochs. Default is 200.")
    parser.add_argument("--seed",
                        type = int,
                        default = 42,
	                    help = "Random seed. Default is 42.")
    parser.add_argument("--dropout",
                        type = float,
                        default = 0.5,
	                    help = "Dropout parameter. Default is 0.5.")
    parser.add_argument("--learning-rate",
                        type = float,
                        default = 0.01,
	                    help = "Learning rate. Default is 0.01.")
    parser.add_argument("--data-set",
                        nargs="?",
                        default='cora',
                        help="The name of data set. Default is cora.")

    parser.add_argument("--community-size",
                        type=int,
                        default=30,
                        help="The size of final community. Default is 30.")

    parser.add_argument("--train-ratio",
                        type=float,
                        default=0.02,
                        help="Test data ratio. Default is 0.02.")

    parser.add_argument("--subgraph-size",
                       type=int,
                       default=400,
                       help="The size of subgraphs. Default is 400. when you try on facebook,it should be smaller")
    parser.add_argument("--layers",
                        type=int,
                        default=[16],
                        nargs='+',
                        help="The size of hidden layers. Default is [16].")
    parser.add_argument("--seed-cnt",
                        type=int,
                        default=20,
                        help="The number of random seeds. Default is 20."
                        )
    parser.add_argument("--iteration",
                        type=bool,
                        default=False,
                        help="Whether to start iteration. Default is False."
                        )
    parser.add_argument("--upsize",
                        type=int,
                        default=20,
                        help="Maximum number of node can be found per iteration. Default is 20."
                        )
    parser.add_argument("--possize",
                        type=int,
                        default=1,
                        help="Incremental train node pairs per iteration. Default is 1."
                        )
    parser.add_argument("--round",
                        type=int,
                        default=11,
                        help="The number of iteration rounds. Default is 10."
                        )
    parser.add_argument("--recommend",
                        type=int,
                        default=0,
                        help="The method of recommendations. Default is 0."
                        )
    #parser.set_defaults(layers = [16, 16, 16])
    #parser.set_defaults(layers=[hidden]*layer_len)
    return parser.parse_args()
