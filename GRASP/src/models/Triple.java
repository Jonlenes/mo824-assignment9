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

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Triple){
            return this.p.equals(((Triple) obj).getP()) && this.d.equals(((Triple) obj).getD()) && this.t.equals(((Triple) obj).getT());
        }
        else
            return false;
    }

    @Override
    public String toString() {
        return "(" + this.getP().toString() + "," + this.getD().toString() + "," + this.getT().toString() + ")";
    }
}