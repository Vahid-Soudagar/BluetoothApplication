package com.example.bluetoothapplication.data;


public class NIBP {
    public int HIGH_PRESSURE_INVALID = 0;
    public int LOW_PRESSURE_INVALID  = 0;

    private int highPressure;
    private int meanPressure;
    private int lowPressure;
    private int cuffPressure;
    private int status;

    public NIBP(int highPressure, int meanPressure, int lowPressure, int cuffPressure, int status) {
        this.highPressure = highPressure;
        this.meanPressure = meanPressure;
        this.lowPressure = lowPressure;
        this.cuffPressure = cuffPressure;
        this.status = status;
    }

    public int getMeanPressure() {
        return meanPressure;
    }

    public int getHighPressure() {
        return highPressure;
    }

    public int getLowPressure() {
        return lowPressure;
    }

    public int getCuffPressure() {
        return cuffPressure;
    }

    public int getStatus() {
        return status;
    }

    public void setHighPressure(int highPressure) {
        this.highPressure = highPressure;
    }

    public void setMeanPressure(int meanPressure) {
        this.meanPressure = meanPressure;
    }

    public void setLowPressure(int lowPressure) {
        this.lowPressure = lowPressure;
    }

    public void setCuffPressure(int cuffPressure) {
        this.cuffPressure = cuffPressure;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return  "Cuff:" +  (cuffPressure!=0 ? cuffPressure: "- -") + "\r\n" +
                "High:"+  (highPressure!=0 ? highPressure: "- -") +
                " Low:" +  (lowPressure !=0 ? lowPressure : "- -") +
                " Mean:"+  (meanPressure!=0 ? meanPressure: "- -");
    }
}
