package dte.mgmprojects.march;

public class DataConf {
    private int Open_P;
    private int W_min;

    public int getOpen_P() {
        return Open_P;
    }

    public void setOpen_P(int open_P) {
        Open_P = open_P;
    }

    public int getW_min() {
        return W_min;
    }

    public void setW_min(int w_min) {
W_min = w_min;
    }

    public DataConf(int Open_P, int W_min){
        this.Open_P = Open_P;
        this.W_min = W_min;
    }
}