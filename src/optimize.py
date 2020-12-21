import pandas as pd
import gurobipy as gp
from gurobipy import GRB, tuplelist, quicksum
from itertools import product
from read_instance import instance_names, read_input

def validate(x, y, P, D, T, S, H, hd, apd, rpt):
    results = {}
    # Problem constraint 1
    results['constraint_1'] = all(
        sum(x[p, d] for p in range(P)) <= 1
        for d in range(D)
    )

    # Problem constraint 3
    results['constraint_3'] = all(
        sum(y[p, d, t] for t in range(T)) == x[p, d] * hd[d]
        for p in range(P) 
        for d in range(D)
    )

    # Problem constraint 4
    results['constraint_4'] = all(
        sum(y[p, d, t] for p in range(P) for d in range(D)) <= S
        for t in range(T)
    )
    
    # Problem constraint 5 e 7
    results['constraint_5'] = all(
        sum(y[p, d, t] for d in range(D)) <= rpt[p, t]
        for p in range(P)
        for t in range(T)
    )

    # Problem constraint 6
    results['constraint_6'] = all(
        sum(hd[d] * x[p, d] for d in range(D)) <= H
        for p in range(P)
    )

    return all(results.values()), results

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

    # Keys
    pd_keys = list(product(range(P), range(D)))
    dt_keys = list(product(range(D), range(T)))
    pt_keys = list(product(range(P), range(T)))
    pdt_keys = list(product(range(P), range(D), range(T)))
    
    # Variables
    x = model.addVars(pd_keys, vtype=GRB.BINARY, name='x')
    y = model.addVars(pdt_keys, vtype=GRB.BINARY, name='y')

    # Objective
    model.setObjective(
        (
            gp.quicksum(apd[p, d] * x[p, d] for p, d in pd_keys)
            + 100 * gp.quicksum(
                gp.quicksum(x[p, d] for p in range(P)) - 1 for d in range(D)
            )
        ),
        GRB.MAXIMIZE,
    )

    # Constraints
    # Problem constraint (1)
    model.addConstrs(
        gp.quicksum(x[p, d] for p in range(P)) <= 1
        for d in range(D)
    )

    # Problem constraint (3)
    model.addConstrs(
        gp.quicksum(y[p, d, t] for t in range(T)) == x[p, d] * hd[d]
        for p, d in pd_keys
    )

    # Problem constraint (4)
    model.addConstrs(
        gp.quicksum(y[p, d, t] for p, d in pd_keys) <= S
        for t in range(T)
    )

    # Problem constraint (5) and (7)
    model.addConstrs(
        gp.quicksum(y[p, d, t] for d in range(D)) <= rpt[p, t]
        for p, t in pt_keys
    )
    
    # Problem constraint (6)
    model.addConstrs(
        gp.quicksum(hd[d] * x[p, d] for d in range(D)) <= H
        for p in range(P)
    )

    return model, x, y

def main():
    # instance_names() --- all them --- or [P50D50S1.pap, P50D50S3.pap, ...]
    # P50D50S1, P50D50S3, P50D50S5, P70D70S1, P70D70S2, P70D70S3
    instances = instance_names()
    timeout = 30*60
    verbose = 1
    
    for instance_name in instances:
        print(f"Running instance {instance_name}")
        instance = read_input(f'instances/{instance_name}')
        model, var_x, var_y = build_model(*instance, timeout, verbose)
        model.optimize()

        # Check if the solution found match problem constraints
        x = model.getAttr("x", var_x)
        y = model.getAttr("x", var_y)
        is_valid, results = validate(x, y, *instance)
        if is_valid:
            print('\tValid solution')
        else:
            print('\tValidate fail:', results)

        pd.DataFrame({
            'instance': instance_name,
            "Z_lb": model.ObjBound,
            "Z_ub": model.objVal,
            "time": model.runtime,
        }, index=[0]).to_csv(f"results/results_{instance_name}.csv", index=False)

if __name__ == "__main__":
    main()
