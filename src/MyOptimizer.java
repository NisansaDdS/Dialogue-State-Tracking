/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import com.joptimizer.functions.*;
import com.joptimizer.optimizers.*;
import com.joptimizer.solvers.*;
import com.joptimizer.util.*;
import java.util.Arrays;

/**
 *
 * @author zw57
 */
public class MyOptimizer {
    
    public int dim = 0;
    public double[] y = null;
        
    public double[] computeBelief()
    {
        dim = y.length;
        double[] x = normalise(y);
        
        
        
        StrictlyConvexMultivariateRealFunction objectiveFunction = new StrictlyConvexMultivariateRealFunction() {

                public double value(double[] X) {
                    double v = 0;
                    for ( int i = 0; i < dim; ++i )
                        if ( X[i] > 0 )
                            v += X[i]*Math.log(X[i]/y[i])+y[i]*Math.log(y[i]/X[i]);
                    return v;
                }

                public double[] gradient(double[] X) {
                    double[] grad = new double[dim];
                    for ( int i = 0; i < dim; ++i )
                        grad[i] = Math.log(X[i]/y[i])+1-y[i]/X[i];
                    return grad;
                }

                public double[][] hessian(double[] X) {
                    double[][] hes = new double[dim][dim];
                    for ( int i = 0; i < dim; ++i )
                        for ( int j = 0; j < dim; ++j )
                        {
                            if ( i != j )
                                hes[i][j] = 0;
                            else
                                hes[i][i] = 1.0/X[i]+y[i]/(X[i]*X[i]);
                        }

                    return hes;
                }

                public int getDim() {
                        return dim;
                }
	};

        // Inquality constraints
        ConvexMultivariateRealFunction[] inequalities = new ConvexMultivariateRealFunction[dim];
        for ( int i = 0; i < dim; ++i )
        {
            double[] a = new double[dim];
            Arrays.fill(a, 0);
            a[i] = -1;
            inequalities[i] = new LinearMultivariateRealFunction(a, 0);
        }

        

        OptimizationRequest or = new OptimizationRequest();
        or.setF0(objectiveFunction);
        or.setInitialPoint(x);
        or.setFi(inequalities);

        double[] a = new double[dim];
        Arrays.fill(a, 1);
        // Equality constraints
        or.setA(new double[][] { a });
        or.setB(new double[] { 1 });
        or.setTolerance(1.E-9);
        or.setMaxIteration(100);

        // optimization
        JOptimizer opt = new JOptimizer();
        opt.setOptimizationRequest(or);
        
        
        
        try {
            int returnCode = opt.optimize();
        } catch ( Exception e ) {
            e.printStackTrace();
        } finally {
            
        }
        
        return opt.getOptimizationResponse().getSolution();
    }
    
    public double[] normalise(double[] v)
    {
        
        double[] x = new double[v.length];
        double sum = 0;
        for ( int i = 0; i < v.length; ++i )
        {
            x[i] = v[i];
            sum += v[i];
        }
        
        for ( int i = 0; i < v.length; ++i )
            x[i] /= sum;
        
        return x;
    }
}
