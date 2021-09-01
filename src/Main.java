import java.io.*;
import java.util.ArrayList;
import java.util.Random;

public class Main {//singleQuery Single BaseStation
    public static double e = 0.001;//错误率
    public static double phi = 0.5;//分位数
    public static int delta_t = 200;//ms
    public static int T = 1000;//ms
    public static int size = 10000000;
    public static String csvFile = "test10000000.txt";


    public static void main(String[] args) throws IOException {

        BufferedWriter bw = new BufferedWriter(new FileWriter("testResult1.txt", true));

        /* Greenwald-Khanna test case
         *
         * double e = 0.25;
         * int[] observations = { 12, 10, 11, 10, 1, 10, 11, 9 };
         * Tuples expected: { (1, 1, 0), (9, 1, 2), (10, 2, 0), (11, 2, 0), (11, 1, 2), (12, 1, 0) }
         *
         * double e = 0.1;
         * int[] observations = {11, 20, 18, 5, 12, 6, 3, 2, 1, 8, 14, 19, 15, 4, 10, 7, 9, 13, 16, 17};
         * Tuples expected: { (1, 1, 0), (3, 2, 0), (5, 2, 0), (7, 1, 2), (8, 2, 0), (9, 1, 2), (11, 2, 0), (12, 1, 0), (13, 1, 2), (14, 1, 1), (16, 1, 2), (17, 1, 2), (18, 2, 0), (20, 2, 0) }
         */


		/*
		double e = 0.25;
		int n = 0;
		ArrayList<Tuple> summary = new ArrayList<Tuple>();
		int[] observations = { 12, 10, 11, 10, 1, 10, 11, 9 };
		for (Integer obs : observations) {
			GK.greenwald_khanna(n, obs, summary, e);
			n++;
		}
		for (Tuple t : summary) {
			System.out.println(t.toString());
		}
		ArrayList<Integer> quantile = GK.quantile(0.5, n, summary, e);
		System.out.println("");
		for (Integer q : quantile) {
			System.out.println(q);
		}
		 */

        /* ========================================================================= */

        /*
         * Merge test case
         *
         * Q'  = { 2:[1..1], 4:[3..4], 8:[5..6], 17:[8..8] }  = { (2, 1, 0), (4, 2, 1), (8, 2, 1), (17, 3, 0) }
         * Q'' = { 1:[1..1], 7:[3..3], 12:[5..6], 15:[8..8] } = { (1, 1, 0), (7, 2, 0), (12, 2, 1), (15, 3, 0) }
         *
         * Q = { 1:[1..1], 2:[2..3], 4:[4..6], 7:[6..8], 8:[8..11], 12:[10..13], 15:[13..15], 17:[16..16] } = { (1, 1, 0), (2, 1, 1), (4, 2, 2), (7, 2, 2), (8, 2, 3), (12, 2, 3), (15, 3, 2), (17, 3, 0)}
         */

		/*
		System.out.println("");
		Tuple t11 = new Tuple(2,1,0);
        Tuple t12 = new Tuple(4,2,1);
        Tuple t13 = new Tuple(8,2,1);
        Tuple t14 = new Tuple(17,3,0);
        Tuple t21 = new Tuple(1,1,0);
        Tuple t22 = new Tuple(7,2,0);
        Tuple t23 = new Tuple(12,2,1);
        Tuple t24 = new Tuple(15,3,0);
        ArrayList<Tuple> s1 = new ArrayList<Tuple>();
        s1.add(0,t11);
        s1.add(1,t12);
        s1.add(2,t13);
        s1.add(3,t14);
        ArrayList<Tuple> s2 = new ArrayList<Tuple>();
        s2.add(0,t21);
        s2.add(1,t22);
        s2.add(2,t23);
        s2.add(3,t24);
        ArrayList<Tuple> s = GKWindow.merge(s1, s2);
        for (Tuple t : s) {
            System.out.println(t.toString());
//        	System.out.println("("+t.getVal()+", "+t.getRmin()+", "+t.getRmax()+")");
        }
		 */

        /* ========================================================================= */


        int n = 0;//当前summary个数
        int blocks = (int) Math.floor(2 / e);

        ArrayList<Block> blist = new ArrayList<Block>(blocks);

        //int[] observations = { 12, 10, 11, 10, 1, 10, 11, 9, 6, 7, 8, 11, 4, 5, 2, 3, 13, 19, 14, 15, 12, 16, 18, 17, 11, 1, 7, 13, 9, 10, 4, 8 };
        ArrayList<Integer> observations = new ArrayList<Integer>();
        Random r = new Random();
        int low = 1, high = 10000000;


        //-------------------------------------------------------------------------------------------------------------------
        int[] arr_data = new int[size + 2];
        long[] arr_time = new long[size + 2];
        int temp = 0;


        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";
        try {
            br = new BufferedReader(new FileReader(csvFile));
            while ((line = br.readLine()) != null) {
                // use comma as separator
                String[] country = line.split(cvsSplitBy);
                arr_data[temp] = Integer.parseInt(country[0]);
                arr_time[temp] = Long.parseLong(country[3]);
                temp++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        long start = System.currentTimeMillis();

        while (n < size) {
            GKWindow.greenwald_khanna_window(n, arr_data[n], size, e, blist);
            n++;
        }
        long end = System.currentTimeMillis();
        ArrayList<Integer> quantile = GKWindow.quantile(phi, n, e, blist);
        for (Integer q : quantile) {
            System.out.print(q + "   ");
        }
        System.out.println("");

        System.out.println(end - start);
        bw.write("\r\n");
        bw.write("dataSize : "+size+" , e = "+e+" ,耗时 "+ (end - start)+ " ms");
        bw.close();



//        bw.write("test data from :"+csvFile);
//        bw.write(", 错误率 e ="+e);
//        bw.write(", 窗口周期 delta_T = "+ delta_t);
//        bw.write(", 周期 T = "+T+"\n");
//        long baseTime = System.currentTimeMillis();
//        long curTime = System.currentTimeMillis() - baseTime;
//        long  delay = baseTime;
//
//        for (int j = 0;j < (int)T/delta_t;j++){
//            while ( arr_time[n] <= delta_t * (j+1) && n < size){
//                if (arr_time[n] <= curTime){
//                    GKWindow.greenwald_khanna_window(n, arr_data[n], size, e, blist);
//                    n++;
////                    System.out.println(n + "  "+curTime);
//                }
//                curTime = System.currentTimeMillis() - baseTime;
//            }
//            ArrayList<Integer> quantile = GKWindow.quantile(phi, n, e, blist);
//           for (Integer q : quantile) {
//                System.out.print(q+ "   ");
//            }
//
//
//
//            long long2 = System.currentTimeMillis();
//            if (long2-baseTime- delta_t * j <= 200){
//                bw.write("第 "+(j+1)+" 个时间窗口 ，耗时 "+200+"ms , ");
//                bw.write("窗口延迟 " + (long2-delay- delta_t )+ " ms, 累计延迟 "+ 0 +" ms");
//            }
//            else {
//                bw.write("第 "+(j+1)+" 个时间窗口 ，耗时 "+(long2-baseTime- delta_t * j)+"ms , ");
//                bw.write("窗口延迟 " + (long2-delay- delta_t )+ " ms, 累计延迟 "+ (long2-baseTime- delta_t * (j+1))+" ms");
//            }
//            bw.write("\r\n");
//            delay = long2;
//
//        }
//        bw.write("\r\n");
//        bw.close();

//            PrintWriter pw = null;
//        try {
//            pw = new PrintWriter("obs.txt");
//            for (int i = 1; i <= size; i++) {
//                int val = r.nextInt(high-low)+low;
//                observations.add(val);
//                pw.println(val);
//            }
//        } catch(Exception err) {
//            err.printStackTrace();
//        } finally {
//            pw.close();
//        }


//        for (Integer obs : observations) {
//            GKWindow.greenwald_khanna_window(n, obs, size, e, blist);
//            n++;
//        }

        //      ArrayList<Tuple> summary = blist.get(0).summary();


//		int rmin = 0, rmax;
//        for (Tuple t : summary) {
//			rmin += t.getG();
//			rmax = rmin + t.getD();
        //          System.out.println(t.toString());
//			System.out.println("("+t.getVal()+", "+rmin+", "+rmax+")");
        //      }

        //       for (int i = 1; i < blist.size(); i++) {

        //          System.out.println("");

//			rmin = 0;			
        //     for (Tuple t : blist.get(i).summary()) {
//				rmin += t.getG();
//				rmax = rmin + t.getD();
        //        System.out.println(t.toString());
//				System.out.println("("+t.getVal()+", "+rmin+", "+rmax+")");
        //    }

        //          summary = GKWindow.merge(summary, blist.get(i).summary());
        //      }

        //System.out.println("");

        //      for (Tuple t : summary) {
        //       System.out.println(t.toString());
//			System.out.println("("+t.getVal()+", "+t.getRmin()+", "+t.getRmax()+")");
        //       }

        // System.out.println("");


        // ====


//        System.out.println("");
//
//        ArrayList<Tuple> s = new ArrayList<Tuple>();
//        n = 0;
//        for (Integer obs : observations) {
//            GK.greenwald_khanna(n, obs, s, e);
//            n++;
//        }
//
////		rmin = 0;
//        for (Tuple t : s) {
////			rmin += t.getG();
////			rmax = rmin + t.getD();
//            System.out.println(t.toString());
////			System.out.println("("+t.getVal()+", "+rmin+", "+rmax+")");
//        }
//
//        ArrayList<Integer> quant = GK.quantile(0.5, n, s, e);
//
//        System.out.println("");
//
//
//        for (Integer q : quant) {
//            System.out.println(q);
//        }
    }
}