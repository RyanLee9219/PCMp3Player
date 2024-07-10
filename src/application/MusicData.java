package application;

public class MusicData {
	private String name;
	private String path;
	
	public MusicData(String name, String path) {
		this.name = name;
		this.path = path;
	}
	
	public String getname() {
		return name;
	}
	
	public String getPath() {
		return path;
	}
	
	@Override
	public String toString() {
		return name;
	}
}
