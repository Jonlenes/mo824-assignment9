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
    hd_updated = [sum(x[p, d] for p in range(P)) for d in range(D)] * hd
    results['constraint_3'] = all(
        sum(y[d, t] for t in range(T)) == hd_updated[d]
        for d in range(D)
    )

    # Problem constraint 4
    results['constraint_4'] = all(
        sum(y[d, t] for d in range(D)) <= S
        for t in range(T)
    )

    # Problem constraint 5
    results['constraint_5'] = all(
        rpt[p, t] != 0 or sum(x[p, d] * y[d, t] for d in range(D)) == rpt[p, t]
        for p in range(P)
        for t in range(T)
    )

    # Problem constraint 6
    results['constraint_6'] = all(
        sum(x[p, d] * y[d, t]
            for d in range(D)
            for t in range(T)
        ) <= H
        for p in range(P)
    )

    # Problem constraint 7
    results['constraint_7'] = all(
        sum(x[p, d] * y[d, t] for d in range(D)) <= 1
        for p in range(P)
        for t in range(T)
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

    # Variables
    x_keys = list(product(range(P), range(D)))
    x = model.addVars(x_keys, vtype=GRB.BINARY, name='x')

    y_keys = list(product(range(D), range(T)))
    y = model.addVars(y_keys, vtype=GRB.BINARY, name='y')

    z_keys = list(product(range(P), range(D), range(T)))
    z = model.addVars(z_keys, vtype=GRB.BINARY, name='z')

    # Objective
    model.setObjective(
        (
            gp.quicksum(apd[p, d] * x[p, d] for p, d in x_keys)
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
        gp.quicksum(y[d, t] for t in range(T)) == 
        gp.quicksum(x[p, d] for p in range(P)) * hd[d]
        for d in range(D)
    )

    # Problem constraint (4)
    model.addConstrs(
        gp.quicksum(y[d, t] for d in range(D)) <= S
        for t in range(T)
    )
    
    # Problem constraint (6)
    model.addConstrs(
        gp.quicksum(hd[d] * x[p, d] for d in range(D)) <= H
        for p in range(P)
    )

    # Problem constraint (5) and (7)
    model.addConstrs(
        gp.quicksum(x[p, d] * y[d, t] for d in range(D)) <= rpt[p, t]
        for p in range(P)
        for t in range(T)
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
            print('Valid solution')
        else:
            print('Validate fail:', results)

        pd.DataFrame({
            'instance': instance_name,
            "Z_lb": model.ObjBound,
            "Z_ub": model.objVal,
            "time": model.runtime,
        }, index=[0]).to_csv(f"results/results_quad_{instance_name}.csv", index=False)

if __name__ == "__main__":
    main()
