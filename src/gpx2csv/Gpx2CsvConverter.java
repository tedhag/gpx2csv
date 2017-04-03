package gpx2csv;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

public class Gpx2CsvConverter {

	private Path csvfile;
	private Path gpxfile;

	public Gpx2CsvConverter(Path gpxfile, Path csvfile) {
		this.gpxfile=gpxfile;
		this.csvfile=csvfile;
	}

	public void execute() {
		System.out.println("hi ho!");
		List<TimeStep> timesteps = new ArrayList<>();
		AtomicReference<TimeStep> cts = new AtomicReference<>();
		try(Stream<String> lines = Files.lines(gpxfile)){
			lines
			.map(l ->{
				return l.trim();
			})
			.forEachOrdered(l -> {
				System.out.println(l);
				if (l.contains("<trkpt lat")) {
					TimeStep current = new TimeStep();
					String[] ss = l.split(" ");
					current.lat = new Double(ss[1].replaceAll("\"", "").split("=")[1]);
					current.lon = new Double(ss[2].replaceAll("\"", "").replaceAll(">", "").split("=")[1]);
					cts.set(current);
					timesteps.add(current);
				}
				
				if (l.startsWith("<time")&&cts.get()!=null) {
					cts.get().timestep = Instant.parse(l.substring("<time>".length(), "<time>2017-04-01T08:10:14.000Z".length()));
				}
				
				if (l.startsWith("<ns3:cad")) {
					String c = l.replaceAll("<ns3:cad>", "").replaceAll("</ns3:cad>", "");
					Integer i = new Integer(c);
					cts.get().cadence = (i==null?0:i);
				}
				
				if (l.startsWith("<ns3:hr")) {
					String c = l.replaceAll("<ns3:hr>", "").replaceAll("</ns3:hr>", "");
					cts.get().hr = new Integer(c);
				}
			});
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		
		TimeStep lastts = null;
		for (int i=0; i<timesteps.size(); i++) {
			TimeStep ts = timesteps.get(i);
			if (lastts==null) {
				ts.distance=0.0;
			}
			else {
				ts.distance = new Haversine(ts.lat, ts.lon, lastts.lat, lastts.lon).calculate().meters();
			}

			lastts=ts;
		}
		
		
		timesteps.stream().forEachOrdered(t -> {
			System.out.println(t.timestep.toString());
			System.out.println(t.hr);
			System.out.println(t.cadence);
			System.out.println(t.cadence*2);
			System.out.println(t.distance);
			
		});
		
		
		try (BufferedWriter p = Files.newBufferedWriter(csvfile)) {
			
			String s = timesteps.stream()
			.map(t -> {
				if (t!=null) return t.timestep.toString()+";"+t.hr+";"+t.cadence*2+";"+t.distance;
				return "";
			})
			.reduce((s1,s2)-> s1+"\n"+s2).get();
			
			try {
				p.append("timestemp;hr;str;meter\n\r");
				p.append(s);
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}  catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	class TimeStep{
		
		public Double lat;
		public Double lon;
		public Instant timestep;
		public Integer hr=0;
		public Integer cadence=0;
		public Double distance=0.0;
	}
	
	public static void main(String[] args) {
		new Gpx2CsvConverter(Paths.get("d:/tmp/gpx/activity_1651760939.gpx"), Paths.get("d:/tmp/gpx/20170401-Melina.csv")).execute();
	}
}
