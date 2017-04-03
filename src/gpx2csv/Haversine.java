package gpx2csv;

public class Haversine {
	final double equatorialEarthRadius = 6378.1370D;
    final double d2r = (Math.PI / 180D);

    private double lat1;
    private double lon1;
    private double lat2;
    private double lon2;
    private double distanceInKm;
    
    public Haversine(double lat1, double lon1, double lat2, double lon2){
    	this.lat1=lat1;
    	this.lon1=lon1;
    	this.lat2=lat2;
    	this.lon2=lon2;
    }
    
    public Haversine calculate() {
    	
        double dlong = (lon2 - lon1) * d2r;
        double dlat = (lat2 - lat1) * d2r;
        double a = Math.pow(Math.sin(dlat / 2D), 2D) + Math.cos(lat1 * d2r) * Math.cos(lat2 * d2r)
                * Math.pow(Math.sin(dlong / 2D), 2D);
        double c = 2D * Math.atan2(Math.sqrt(a), Math.sqrt(1D - a));
        double d = equatorialEarthRadius * c;

        distanceInKm= d;
        return this;
    }
    
    public double kilometers(){
    	return distanceInKm;
    }
    
    public double meters(){
    	return  1000*distanceInKm;
    }
    
    

}
