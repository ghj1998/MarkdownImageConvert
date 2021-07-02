import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class MarkdownImageConvert {

    public static List<String> filePaths;

    static {
        filePaths = new ArrayList<>();
    }

    // 对目录下每个文件调用`fileConvert`方法
    public static void main(String[] args) throws IOException {
        // 修改此处的参数名为要遍历修改的目录名
        File directory = new File("目录名");
        getFilesInDirectory(directory, filePaths);
        for (String filePath :
                filePaths) {
            System.out.println("正在处理文件："+filePath);
            fileConvert(filePath);
        }
    }

    // 递归扫描目录下的所有文件，并将文件的绝对地址保存在`filePaths`列表中
    public static void getFilesInDirectory(File directory, List<String> filePaths) {
        for (File f :
                directory.listFiles()) {
            if (f.isDirectory()) {
                getFilesInDirectory(f, filePaths);
            } else {
                filePaths.add(f.getAbsolutePath());
            }
        }
    }

    // 文件读取并修改其中的图片链接
    public static void fileConvert(String filePath) throws IOException {
        List<String> strings = Files.readAllLines(Paths.get(filePath));
        for (String s : strings) {
            if (s.length() == 0) {
                continue;
            }
            if (s.trim().startsWith("![")) {
                String imageAddress = s.substring(s.indexOf('(') + 1, s.length() - 1);
                if (imageAddress.startsWith("https://raw.githubusercontent.com")) {
                    convert(imageAddress, strings, s);
                }
            }
            if (s.trim().startsWith("<img")) {
                String imageAddress = s.substring(s.indexOf('"') + 1, s.indexOf('"', s.indexOf('a') - 2));
                convert(imageAddress, strings, s);
            }
        }
        Files.write(Paths.get(filePath), strings);
    }
    
    public static void convert(String imageAddress, List<String> strings, String s) throws IOException {
        String imageName = imageAddress.substring(imageAddress.lastIndexOf('/') + 1);
        System.out.println("正在处理图片的链接为："+imageAddress + '\n');
        URL url = new URL(imageAddress);
        URLConnection connection = url.openConnection();
        InputStream inputStream = connection.getInputStream();
        File file = new File(imageName);
        byte[] buffer = new byte[256];
        file.createNewFile();
        FileOutputStream outputStream = new FileOutputStream(imageName);
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, len);
        }
        inputStream.close();
        outputStream.close();
        strings.set(strings.indexOf(s), String.format("![%s](%s)",imageName, file.getAbsolutePath()));
    }
}
