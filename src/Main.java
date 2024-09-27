import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Main {
    private static FileNode rootDirectory;
    private static List<FileNode> files;

    private static void initializeRootDirectory(String pathToRootDirectory) {
        Path path = Paths.get(pathToRootDirectory);
        if (!Files.exists(path)) {
            throw new IllegalArgumentException("The path does not exist: " + pathToRootDirectory);
        }
        if (!Files.isDirectory(path)) {
            throw new IllegalArgumentException("Not a directory: " + pathToRootDirectory);
        }
        System.out.println(path.toUri());
        File root = new File(path.toUri());
        rootDirectory = new FileNode("", root.getAbsolutePath(), root.getName(), true);
        files = new LinkedList<>();
        scanDirectory(root.list(), rootDirectory);
        addRequireFiles(rootDirectory);
    }

    private static void scanDirectory(String[] childrenNames, FileNode parentDirectory) {
        for (String childName : childrenNames) {
            File file = new File(parentDirectory.getAbsolutePath() + "/" + childName);
            String relativePath = (parentDirectory.getRelativePath().length() > 0 ? parentDirectory.getRelativePath() + "/" : "") + file.getName();
            FileNode fileNode;
            if (file.isFile()) {
                fileNode = parseToFile(file, relativePath);
                files.add(fileNode);
            } else {
                fileNode = new FileNode(relativePath, file.getAbsolutePath(), file.getName(), true);
                scanDirectory(file.list(), fileNode);
            }
            parentDirectory.addChild(fileNode);
        }
    }

    private static FileNode parseToFile(File file, String relativePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String requirePrefix = "require ‘";
            String line;
            StringBuilder content = new StringBuilder();
            List<String[]> requires = new LinkedList<>();
            while ((line = reader.readLine()) != null) {
                if (line.startsWith(requirePrefix)) {
                    requires.add(line.substring(requirePrefix.length(), line.length() - 1).split("/"));
                }
                content.append(line).append('\n');
            }
            return new FileNode(relativePath,file.getAbsolutePath(), file.getName(), content.toString(), requires);
        } catch (Exception e) {
            throw new RuntimeException(e.getCause());
        }
    }

    private static void addRequireFiles(FileNode fileNode) {
        for (FileNode child : fileNode.getChildren()) {
            if (!child.isDirectory()) {
                for (String[] path : child.getRequirePaths()) {
                    child.addRequireFile(findFile(path));
                }
            } else {
                addRequireFiles(child);
            }
        }
    }

    private static FileNode findFile(String[] path) {
        FileNode current = rootDirectory;
        int index = 0;
        while (index < path.length) {
            for (FileNode next : current.getChildren()) {
                if (next.getName().equals(path[index])) {
                    current = next;
                    index++;
                    break;
                }
            }
        }
        return current;
    }

    private static void cycleFinder(FileNode fileNode) {
        for (FileNode child : fileNode.getChildren()) {
            if (child.isDirectory()) {
                cycleFinder(child);
            } else if (!child.isChecked()) {
                cycleCheck(child, child.getRelativePath());
            }
        }
    }

    private static void cycleCheck(FileNode file, String requireChain) {
        file.setVisited(true);
        file.setChecked(true);
        for (FileNode requireFile : file.getRequireFiles()) {
            if (requireFile.isVisited()) {
                throw new RuntimeException("Cycle found: " + requireChain + " -> " + requireFile.getRelativePath());
            } else {
                cycleCheck(requireFile, requireChain + " -> " + requireFile.getRelativePath());
            }
        }
        file.setVisited(false);
    }

    private static void calculatePriority() {
        for (FileNode file : files) {
            file.setPriority(calculatePriority(file, 0));
            for (FileNode fileNode : files) {
                fileNode.setDepth(0);
            }
        }
    }

    private static int calculatePriority(FileNode file, int priority) {
        int maxPriority = priority;
        for (FileNode child : file.getRequireFiles()) {
            if (child.getDepth() < priority + 1) {
                child.setDepth(priority + 1);
                maxPriority = Math.max(maxPriority, calculatePriority(child, priority + 1));
            }
        }
        return maxPriority;
    }

    private static void printStructure() {
        Queue<FileNode> queue = new LinkedList<>();
        queue.add(rootDirectory);
        while (!queue.isEmpty()) {
            System.out.println(queue.peek().getName());
            System.out.println("+-----------------+");
            for (FileNode child : queue.peek().getChildren()) {
                System.out.println(child.getName() + (child.isDirectory() ? " [+]" : " Priority: " + child.getPriority()));
                if (child.isDirectory()) {
                    queue.add(child);
                }
            }
            System.out.println("+-----------------+\n");
            queue.poll();
        }
    }

    private static void printList() {
        files.forEach(fileNode -> System.out.println(fileNode.getRelativePath()));
    }

    private static void concatenateContents() {
        try (FileWriter writer = new FileWriter("output")) {
            for (FileNode file : files) {
                writer.append(file.getContent());
            }
            writer.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void run(String pathToRootDirectory) {
        initializeRootDirectory(pathToRootDirectory);
        cycleFinder(rootDirectory);
        calculatePriority();
        //printStructure();
        files.sort(FileNode::compareTo);
        printList();
        concatenateContents();
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Please specify an absolute path to a root directory." +
                    "\nFor example: java Main \"D:\\root\"");
        } else {
            run(args[0]);
        }
    }
}
