import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Random;


public class SingleRun {
    public static double phi = 0.5;//分位数
    public static int delta_t = 200;//ms
    public static int T = 1000;//ms
    public static double errorBound;

    public static int[] run(ArrayList<BaseStation> arr, Query query) throws IOException {
        errorBound = query.getErrorBound();
        double[] upper = new double[arr.size()];
        double[] upper_EPS = new double[arr.size()];
        double[] upper_UTC = new double[arr.size()];
        double[] upper_Random = new double[arr.size()];
        double[] upper_OEP = new double[arr.size()];
        for (int i = 0; i < arr.size(); i++) {
            upper[i] = computeDataUpperBound(errorBound, arr, i);
        }
        double[] yita = dataReDistribution(arr, errorBound, upper);
        double[] yita_EPS = dataReDistributionEpsFirst(arr, errorBound, upper_EPS);
        double[] yita_UTC = dataReDistributionUTCFirst(arr, errorBound, upper_UTC);
        double[] yita_Random = dataReDistributionRandom(arr, errorBound, upper_Random);
        double[] yita_OEP = dataReDistributionOEP(arr, errorBound, upper_OEP);

        int dataSize = query.dataSize;

        String csvFile1 = "test25000000.txt";
        int[] delay = execute(arr, yita, upper, dataSize, csvFile1, 1);
//        execute(arr, yita_EPS, upper_EPS, dataSize, csvFile1, 2);
//        execute(arr, yita_UTC, upper_UTC, dataSize, csvFile1, 3);
//        execute(arr, yita_Random, upper_Random, dataSize, csvFile1, 4);
//        execute(arr, yita_OEP, upper_OEP, dataSize, csvFile1, 5);

        BufferedWriter bw = new BufferedWriter(new FileWriter("FinalSingleTestResult.txt", true));
        bw.write("\r\n");
        bw.write("-------------------------------------------------------------------------------------------");
        bw.write("\r\n");
        bw.close();
        return delay;

    }

    public static int[]  execute(ArrayList<BaseStation> arr, double[] yita, double[] upper, int dataSize, String csvFile1, int type) throws IOException {
        double temp = 0;
        if (type == 1) {
            arr.sort(Comparator.comparingDouble(BaseStation::getId));
        } else if (type == 2) {
            arr.sort(Comparator.comparingDouble(BaseStation::getE));
        } else if (type == 3) {
            arr.sort(Comparator.comparingDouble(BaseStation::getUTC));
        } else if (type == 4) {
            arr.sort(Comparator.comparingDouble(BaseStation::getLatitude));
        } else if (type == 5) {
            arr.sort(Comparator.comparingDouble(BaseStation::getLongitude));
        }
        int []res = new int[2];
        int delay = 0;
        int mem = 0;
        for (int i = 0; i < arr.size(); i++) {
            if (yita[i] == upper[i]) {
                res= Run.run(arr.get(i), (int) (dataSize * yita[i]), csvFile1, 0.5, (int) temp * dataSize, yita[i], 0, type, upper[i]);
            } else {
                res = Run.run(arr.get(i), (int) (dataSize * yita[i]), csvFile1, 0.5, (int) temp * dataSize, yita[i], 1, type, upper[i]);
            }
            temp = temp + yita[i];
            if (delay < res[0]){
                delay = res[0];
            }
            mem = mem+res[1];

        }
        double errorRate = 0;
        for (int i = 0; i < arr.size(); i++) {
            errorRate = errorRate + yita[i] * arr.get(i).getE();
        }
        BufferedWriter bw = new BufferedWriter(new FileWriter("FinalSingleTestResult.txt", true));
        bw.write("\r\n");
        bw.write("ErrorRate: " + errorRate);
        bw.write("\r\n");
        bw.close();
        res[0] = delay;
        res[1] = mem;

        return res;
    }

    public static double[] dataReDistribution(ArrayList<BaseStation> arr, double errorBound, double[] upper) {
        int[] set = new int[arr.size()];
        for (int i = 0; i < arr.size(); i++) {
            set[i] = 0;
        }
        arr.sort(Comparator.comparingDouble(BaseStation::getId));//使之有序
        for (int i = 0; i < arr.size(); i++) {
            upper[i] = computeDataUpperBound(errorBound, arr, i);
        }
        double yita = 1;
        double[] yita_final = new double[arr.size()];
        while (yita > 0) {
            double z = 0;
            for (int i = 0; i < arr.size(); i++) {
                if (set[i] == 0) {
                    z = z + 1 / arr.get(i).getUTC();
                }
            }
            boolean flag = true;
            for (int i = 0; i < arr.size(); i++) {
                if (set[i] == 0) {
                    double yita_i = Math.round(yita * 100 / (arr.get(i).getUTC() * z)) / 100.0;
                    if (arr.get(i).getE() > errorBound && yita_i > upper[i]) {
                        yita_final[i] = upper[i];
                        set[i] = 1;
                        flag = false;
                        yita = yita - upper[i];
                    } else {
                        yita_final[i] = yita_i;
                    }
                }
            }
            if (flag) {
                break;
            }
        }
        return yita_final;
    }


    public static double[] dataReDistributionUTCFirst(ArrayList<BaseStation> arr, double errorBound, double[] upper) {  //多出来的放到UTC最小的去
        int[] set = new int[arr.size()];
        for (int i = 0; i < arr.size(); i++) {
            set[i] = 0;
        }
        double yita = 1;
        double[] yita_final = new double[arr.size()];
        double z = 0;
        arr.sort(Comparator.comparingDouble(BaseStation::getUTC));//使之有序
        for (int i = 0; i < arr.size(); i++) {
            upper[i] = computeDataUpperBound(errorBound, arr, i);
        }
        for (int i = 0; i < arr.size(); i++) {
            if (set[i] == 0) {
                z = z + 1 / arr.get(i).getUTC();
            }
        }
        for (int i = 0; i < arr.size(); i++) {
            if (set[i] == 0) {
                double yita_i = Math.round(100 / (arr.get(i).getUTC() * z)) / 100.0;
                if (arr.get(i).getE() > errorBound && yita_i > upper[i]) {
                    yita_final[i] = upper[i];
                    set[i] = 1;
                    yita = yita - upper[i];
                } else {
                    yita_final[i] = yita_i;
                    yita = yita - yita_i;
                }
            }
        }
        for (int i = 0; i < arr.size(); i++) {
            if (set[i] == 0 && yita > 0) {
                if (yita <= upper[i] - yita_final[i]) {
                    yita_final[i] = yita_final[i] + yita;
                    yita = 0;
                } else {
                    yita = yita - upper[i] + yita_final[i];
                    yita_final[i] = upper[i];
                }
            }
        }
        return yita_final;
    }


    public static double[] dataReDistributionEpsFirst(ArrayList<BaseStation> arr, double errorBound, double[] upper) {  //多出来的放到Eps最小的去
        int[] set = new int[arr.size()];
        for (int i = 0; i < arr.size(); i++) {
            set[i] = 0;
        }
        double yita = 1;
        double[] yita_final = new double[arr.size()];
        double z = 0;
        arr.sort(Comparator.comparingDouble(BaseStation::getE));//使之有序
        for (int i = 0; i < arr.size(); i++) {
            upper[i] = computeDataUpperBound(errorBound, arr, i);
        }
        for (int i = 0; i < arr.size(); i++) {
            if (set[i] == 0) {
                z = z + 1 / arr.get(i).getUTC();
            }
        }
        for (int i = 0; i < arr.size(); i++) {
            if (set[i] == 0) {
                double yita_i = Math.round(100 / (arr.get(i).getUTC() * z)) / 100.0;
                if (arr.get(i).getE() > errorBound && yita_i > upper[i]) {
                    yita_final[i] = upper[i];
                    set[i] = 1;
                    yita = yita - upper[i];
                } else {
                    yita_final[i] = yita_i;
                    yita = yita - yita_i;
                }
            }
        }
        for (int i = 0; i < arr.size(); i++) {
            if (set[i] == 0 && yita > 0) {
                if (yita <= upper[i] - yita_final[i]) {
                    yita_final[i] = yita_final[i] + yita;
                    yita = 0;
                } else {
                    yita = yita - upper[i] + yita_final[i];
                    yita_final[i] = upper[i];
                }
            }
        }
        return yita_final;
    }


    public static double[] dataReDistributionRandom(ArrayList<BaseStation> arr, double errorBound, double[] upper) {  //多出来的随机放
        int[] set = new int[arr.size()];
        for (int i = 0; i < arr.size(); i++) {
            set[i] = 0;
        }
        arr.sort(Comparator.comparingDouble(BaseStation::getLatitude));//使之有序
        for (int i = 0; i < arr.size(); i++) {
            upper[i] = computeDataUpperBound(errorBound, arr, i);
        }


        double yita = 1;
        double[] yita_final = new double[arr.size()];
        double z = 0;
        for (int i = 0; i < arr.size(); i++) {
            if (set[i] == 0) {
                z = z + 1 / arr.get(i).getUTC();
            }
        }
        for (int i = 0; i < arr.size(); i++) {
            if (set[i] == 0) {
                double yita_i = Math.round(100 / (arr.get(i).getUTC() * z)) / 100.0;
                if (arr.get(i).getE() > errorBound && yita_i > upper[i]) {
                    yita_final[i] = upper[i];
                    set[i] = 1;
                    yita = yita - upper[i];
                } else {
                    yita_final[i] = yita_i;
                    yita = yita - yita_i;
                }
            }
        }
        for (int i = 0; i < arr.size(); i++) {
            if (set[i] == 0 && yita > 0) {
                if (yita <= upper[i] - yita_final[i]) {
                    yita_final[i] = yita_final[i] + yita;
                    yita = 0;
                } else {
                    yita = yita - upper[i] + yita_final[i];
                    yita_final[i] = upper[i];
                }
            }
        }
        return yita_final;
    }

    public static double computeDataUpperBound(double errorBound, ArrayList<BaseStation> arr, int i) {
        double e_min = 1;
        for (int j = 0; j < arr.size(); j++) {
            if (e_min >= arr.get(j).getE() && j != i) {
                e_min = arr.get(j).getE();
            }
        }
        if (arr.get(i).getE() > errorBound) {
            return Math.round((errorBound - e_min) * 100 / (arr.get(i).getE() - e_min)) / 100.0;
        } else {
            return 1;
        }
    }


    public static double[] dataReDistributionOEP(ArrayList<BaseStation> arr, double errorBound, double[] upper) {  //多出来的随机放
        int[] set = new int[arr.size()];
        for (int i = 0; i < arr.size(); i++) {
            set[i] = 0;
        }
        arr.sort(Comparator.comparingDouble(BaseStation::getLongitude));//使之有序
        for (int i = 0; i < arr.size(); i++) {
            upper[i] = computeDataUpperBound(errorBound, arr, i);
        }


        double yita = 1;
        double[] yita_final = new double[arr.size()];
        double z = 0;
        for (int i = 0; i < arr.size(); i++) {
            if (set[i] == 0) {
                z = z + 1 / arr.get(i).getUTC();
            }
        }
        for (int i = 0; i < arr.size(); i++) {
            if (set[i] == 0) {
                double yita_i = Math.round(100 / (arr.get(i).getUTC() * z)) / 100.0;
                if (arr.get(i).getE() > errorBound && yita_i > upper[i]) {
                    yita_final[i] = upper[i];
                    set[i] = 1;
                    yita = yita - upper[i];
                } else {
                    yita_final[i] = yita_i;
                    yita = yita - yita_i;
                }
            }
        }
        for (int i = 0; i < arr.size(); i++) {
            if (set[i] == 0 && yita > 0) {
                if (yita <= upper[i] - yita_final[i]) {
                    yita_final[i] = yita_final[i] + yita;
                    yita = 0;
                } else {
                    yita = yita - upper[i] + yita_final[i];
                    yita_final[i] = upper[i];
                }
            }
        }
        return yita_final;
    }
}
