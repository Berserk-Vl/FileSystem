import java.util.LinkedList;
import java.util.List;

public class FileNode implements Comparable<FileNode> {
    private final String name;
    private final String absolutePath;
    private final String relativePath;
    private String content;
    private List<String[]> requirePaths;
    private List<FileNode> requireFiles;
    private final boolean directory;
    private final List<FileNode> children;
    private boolean visited;
    private boolean checked;
    private int priority;
    private int depth;


    public FileNode(String relativePath, String absolutePath, String name, boolean directory) {
        this.relativePath = relativePath;
        this.absolutePath = absolutePath;
        this.name = name;
        this.directory = directory;
        content = null;
        requirePaths = null;
        requireFiles = null;
        children = new LinkedList<>();
        visited = false;
        checked = false;
        priority = 0;
        depth = 0;
    }

    public FileNode(String relativePath, String absolutePath, String name, String content, List<String[]> requirePaths) {
        this(relativePath,absolutePath, name, false);
        this.content = content;
        this.requirePaths = requirePaths;
        requireFiles = new LinkedList<>();
    }


    public String getAbsolutePath() {
        return absolutePath;
    }

    public String getRelativePath() {
        return relativePath;
    }

    public String getName() {
        return name;
    }

    public String getContent() {
        return content;
    }

    public List<String[]> getRequirePaths() {
        return requirePaths;
    }

    public List<FileNode> getRequireFiles() {
        return requireFiles;
    }

    public void addRequireFile(FileNode requireFile) {
        requireFiles.add(requireFile);
    }

    public List<FileNode> getChildren() {
        return children;
    }

    public void addChild(FileNode child) {
        children.add(child);
    }

    public boolean isDirectory() {
        return directory;
    }

    public boolean isVisited() {
        return visited;
    }

    public void setVisited(boolean visited) {
        this.visited = visited;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    @Override
    public int compareTo(FileNode o) {
        return priority == o.getPriority() ? name.compareTo(o.name) : priority - o.priority;
    }
}
