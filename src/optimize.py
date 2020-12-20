import pandas as pd
import gurobipy as gp
from gurobipy import GRB, tuplelist, quicksum
from datetime import datetime as dt
from read_instance import instance_names, read_input

def build_model(P, D, T, S, H, hd, apd, rpt, timeout, verbose=0):
    """
    Build the PAP model
    """
    # Create a new model
    model = gp.Model("PAP")

    # Set Params
    model.setParam(gp.GRB.Param.OutputFlag, verbose)
    model.setParam(gp.GRB.Param.TimeLimit, timeout)
    model.setParam(gp.GRB.Param.Seed, 42)

    return model

def main():
    # instance_names() --- all them --- or [P50D50S1.pap, P50D50S3.pap, ...]
    instances = instance_names()
    timeout = 30*60
    verbose = 0
    
    for instance_name in instances:
        print(f"Running instance {instance_name}")
        instance = read_input(f'instances/{instance_name}')
        model = build_model(*instance, timeout, verbose)
        model.optimize()

        pd.DataFrame({
            'instance': instance_name,
            "Z_lb": model.ObjBound,
            "Z_ub": model.objVal,
            "time": model.runtime,
        }, index=[0]).to_csv(f"results/results_{instance_name}.csv", index=False)

if __name__ == "__main__":
    main()
