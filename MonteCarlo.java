/**
 * Created by Juliet on 5/7/14.
 */

import java.util.*;
import javafx.util.Pair;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.joda.time.DateTime;
import org.apache.commons.math3.*;


public class MonteCarlo {
    /*
     *class RandomVector
     *this class is used to build an array consists of 252 random numbers.
     *the random numbers are build on the gaussian.
     *so by function getVector(), we can a list of 252 random numbers.
     */
    static class RandomVector implements RandomVectorGenerator{
        private Random myRandom = new Random();
        private Double[] myRanList = new Double[252];
        public Double[] getVector (){
            for (int i=0;i<252;i++){
                myRanList[i]=myRandom.nextGaussian();
            }
            return myRanList;
        }
    }

    /*
     * the RandomVector myvector is used by the whole program, is a global Randomvector.
     * myranlist is the result of myvector.getVector().
     */

    static public RandomVector myvector= new RandomVector();
    static public Double[] myranlist;

    /*
     * the function antiThetic
     * this function is used to get the opposite of the randomlist
     * In another words, is to get multiple -1 to every random number in randomlist.
     * It takes in a integer count.
     * if count is a even number, this function do the opposite.
     * else if the count is a odd number, the function do nothing
     * It returns an array consists 255 double.
     */
    public static Double[] antiThetic (int count) {
        if (count%2!=0){
            for (int i=0; i<252;i++){
                myranlist[i]= -myranlist[i];
            }
        }
        else myranlist=myvector.getVector();
        return myranlist;
    }

    /*
     *class Stock
     * this class implements StockPath
     * Stock contains three internal objects, S0, volatility and r
     * to initialize a stock, one need to input these three objects
     * the function getPrices() is used to get a stockpath
     * getPrices has no input. It create a list, return an ArrayList<Pair<DateTime,Double>>
     * In the arraylist, consists of several pairs
     * In each pairs, it has key as a datetime and the value is the stock price
     * To get the stock price, we need the stock the price of the day before.
     * Using the formula S = tempS*Math.exp((r-volatility*volatility/2.0)+volatility*z)
     * After get the stock price, add the pair into the arraylist
     * After done all, there are 253 pairs in the arraylist, and return the arraylist
     */

    static class Stock implements StockPath{
        private Double S0;
        private Double volatility;
        public Double r;
        public Stock(Double S0, Double volatility, Double r){
            this.S0=S0;
            this.volatility=volatility;
            this.r=r;
        }
        public ArrayList<Pair<DateTime,Double>> getPrices(){
            ArrayList<Pair<DateTime,Double>> pairs =new ArrayList<Pair<DateTime,Double>>();
            DateTime date= new DateTime();
            Pair<DateTime,Double> pair= new Pair<DateTime, Double>(date,S0);
            pairs.add(pair);
            Double tempS=S0;
            for(int i=0;i<252;i++){
                date=date.plusDays(1);
                Double z = myranlist[i];
                Double S = tempS*Math.exp((r-volatility*volatility/2.0)+volatility*z);
                tempS=S;
                Pair<DateTime,Double> p= new Pair<DateTime, Double>(date,S);
                pairs.add(p);
            }
            return pairs;
        }

    }

    /*
     * class callOption( European )
     * this class implements the interface PayOut
     * this class has an attribute called K, is the strike price
     * the function getPayout is to get the last day's stock price
     * and then minus the strike price, if the result is larger then zero, return the result
     * else return zero
     * this is the European style
     */
    static class callOption implements PayOut{
        private Double K;
        public callOption (Double K){
            this.K= K;
        }
        public Double getPayout(StockPath path){
            ArrayList<Pair<DateTime,Double>> pairs =new ArrayList<Pair<DateTime,Double>>();
            pairs =path.getPrices();
            int i=pairs.size()-1;
            Pair<DateTime,Double> pair=pairs.get(i);
            Double price=pair.getValue();
            if ((price-K)>=0) return price-K;
            else return 0.0;
        }
    }

    /*
     * class lookBackCall
     * this class implements the interface PayOut
     * this class has an attribute called K, is the strike price
     * the function getPayout is to get the largest stock price
     * to get the largest stock price, we need to go though the arraylist returned from the getPrices.
     * and then minus the strike price, if the result is larger then zero, return the result
     * else return zero
     */
    static class lookBackCall implements PayOut{
        private Double K;
        public lookBackCall (Double K){
            this.K= K;
        }
        public Double getPayout(StockPath path){
            ArrayList<Pair<DateTime,Double>> pairs =new ArrayList<Pair<DateTime,Double>>();
            pairs =path.getPrices();
            Double price=Double.MIN_VALUE;
            for (int i=0;i<pairs.size();i++){
                Pair<DateTime,Double> pair=pairs.get(i);
                Double temp=pair.getValue();
                if (temp>price) price=temp;
            }
            if ((price-K)>=0) return price-K;
            else return 0.0;
        }
    }

    /*
     * class lookBackPut
     * this class implements the interface PayOut
     * this class has an attribute called K, is the strike price
     * the function getPayout is to get the smallest stock price
     * to get the smallest stock price, we need to go though the arraylist returned from the getPrices.
     * and then be minusd by the strike price, if the result is larger then zero, return the result
     * else return zero
     */
    static class lookBackPut implements PayOut{
        private Double K;
        public lookBackPut (Double K){
            this.K= K;
        }
        public Double getPayout(StockPath path){
            ArrayList<Pair<DateTime,Double>> pairs =new ArrayList<Pair<DateTime,Double>>();
            pairs =path.getPrices();
            Double price=Double.MAX_VALUE;
            for (int i=0;i<pairs.size();i++){
                Pair<DateTime,Double> pair=pairs.get(i);
                Double temp=pair.getValue();
                if (temp<price) price=temp;
            }
            if ((K-price)>=0) return K-price;
            else return 0.0;
        }
    }

    /*
     * class asianCall
     * this class implements the interface PayOut
     * this class has an attribute called K, is the strike price
     * the function getPayout is to get the average stock price
     * to get the average stock price, we need to go though the arraylist returned from the getPrices, and add up the stock prices. then divided it with the size of the stock path, which is 253
     * and then minus the strike price, if the result is larger then zero, return the result
     * else return zero
     */
    static class asianCall implements PayOut{
        private Double K;
        public asianCall (Double K){
            this.K= K;
        }
        public Double getPayout(StockPath path){
            ArrayList<Pair<DateTime,Double>> pairs =new ArrayList<Pair<DateTime,Double>>();
            pairs =path.getPrices();
            Double price=0.0;
            for (int i=0;i<pairs.size();i++){
                Double temp=pairs.get(i).getValue();
                price+=temp;
            }
            price=price/pairs.size();
            if ((price-K)>=0) return price-K;
            else return 0.0;
        }
    }

    /*
     *the function Track
     * this function inputs a stock class object, a payout class object and a double
     * track first calculate the value of y using the input double, probability, using a NormalDistribution object, and usingdistribution.inverseCumulativeProbability((probability + 1) / 2), get y
     * then calculate the payout of the stock, every time after execuate the payout, calculate y*Math.sqrt(o2/i)>0.01
     * o2 is calculated though (x2*i+x*x)/(i+1)-(u*i+x)/(i+1)*(u*i+x)/(i+1);
     * if it is true, do the whole things again, until it turns to false.
     * When it is false, return u.
     * To get more accurate result, the whole process need to run 10000 times before judging to be run again or not.
     */
    public static Double track (Stock myStock,PayOut myPayout,Double probability){
        NormalDistribution distribution=new NormalDistribution();
        Double y=distribution.inverseCumulativeProbability((probability + 1) / 2);
        Double u=0.0;
        Double x2=0.0;
        Double x=0.0;
        int i=0;
        Double o2=0.0;
        while (i<10000||y*Math.sqrt(o2/i)>0.01){
            myranlist= antiThetic(i);
            x=myPayout.getPayout(myStock);
            u=(u*i+x)/(i+1);
            x2=(x2*i+x*x)/(i+1);
            o2=x2-u*u;
            i++;
        }
        return u;

    }

    /*
     *the main function
     * this function is to test
     * there are two problems
     * both use the same stock, mystock1=Stock(152.35,0.01,0.0001);
     * but different payout, the first question is using the European payout
     * the second question is to use the asian payout.
     * We will print the result, of the option and the price
     * the option is the u returned in getPayout function
     * the price is calculate by Math.exp(-mystock1.r*252)*option.
     */
    public static void main(String[] args){

        Stock mystock1= new Stock(152.35,0.01,0.0001);
        callOption mypayout1= new callOption(165.0);
        Double option= track(mystock1,mypayout1,0.96);
        Double price=Math.exp(-mystock1.r*252)*option;
        System.out.println("#1:");
        System.out.println("option= "+option);
        System.out.println("price= "+price);
        asianCall mypayout2= new asianCall(164.0);
        option=track(mystock1,mypayout2,0.96);
        price=Math.exp(-mystock1.r*252)*option;
        System.out.println("#2:");
        System.out.println("option= "+option);
        System.out.println("price= "+price);
    }

}
