package models;

public class Triple {
    private Integer p;
    private Integer d;
    private Integer t;
    
    public Triple(Integer p, Integer d, Integer t) {
        this.p = p;
        this.d = d;
        this.t = t;
    }
    public Integer getP() {
        return p;
    }
    public Integer getD() {
        return d;
    }
    public Integer getT() {
        return t;
    }
    public void setP(Integer p) {
        this.p = p;
    }
    public void setD(Integer d) {
        this.d = d;
    }
    public void setT(Integer t) {
        this.t = t;
    }
    
}