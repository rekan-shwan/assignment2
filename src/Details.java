import java.io.File;

public class Details {
    private File path;

    public boolean setPath(String path) {
        this.path = new File(path);
        if (this.path.exists() && this.path.isDirectory()) {
            return true;
        } else {
            
            return false;
        }
    }

    public File getPath() {
        return path;
    }
}