package reposense.system;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import reposense.util.Constants;


public class CommandRunner {

    private static boolean isWindows = isWindows();
    private static byte[] buffer = new byte[1 << 13];

    public static String gitLog(String root, Date fromDate, Date toDate) {
        File rootFile = new File(root);
        String command = "git log --no-merges ";
        if (fromDate != null) {
            command += " --since=\"" + Constants.GIT_LOG_DATE_FORMAT.format(fromDate) + "\" ";
        }
        if (toDate != null) {
            command += " --until=\"" + Constants.GIT_LOG_DATE_FORMAT.format(toDate) + "\" ";
        }
        command += " --pretty=format:\"%h|%aN|%ad|%s\" --date=iso --shortstat -- \"*.java\" -- \"*.adoc\"";
        return runCommand(rootFile, command);
    }

    public static void checkOut(String root, String hash) {
        File rootFile = new File(root);
        runCommand(rootFile, "git checkout " + hash);
    }

    public static String blameRaw(String root, String fileDirectory) {
        File rootFile = new File(root);
        return runCommand(rootFile, "git blame " + addQuote(fileDirectory) + " -w -C -C -M --line-porcelain");
    }

    public static String checkStyleRaw(String absoluteDirectory) {
        File rootFile = new File(".");
        return runCommand(
                rootFile,
                "java -jar checkstyle-7.7-all.jar -c /google_checks.xml -f xml " + absoluteDirectory
        );
    }

    public static String cloneRepo(String org, String repoName) {
        File rootFile = new File(Constants.REPOS_ADDRESS + "/" + org);
        if (!rootFile.exists()) {
            rootFile.mkdirs();
        }
        return runCommand(rootFile, "git clone " + Constants.GITHUB_URL_ROOT + org + "/" + repoName);
    }



    private static String runCommand(File directory, String command) {
        ProcessBuilder pb = null;
        if (isWindows) {
            pb = new ProcessBuilder()
                    .command(new String[]{"CMD", "/c", command})
                    .directory(directory);
        } else {
            pb = new ProcessBuilder()
                    .command(new String[]{"bash", "-c", command})
                    .directory(directory);
        }
        Process p;
        int exit = 0;
        try {
            p = pb.start();
            exit = p.waitFor();
        } catch (IOException ioe) {
            throw new RuntimeException("Error Creating Process:" + ioe.getMessage());
        } catch (InterruptedException ie) {
            throw new RuntimeException("Error Handling Process.");
        }

        if (exit == 0) {
            return convertToString(p.getInputStream());
        } else {
            String errorMessage = "Error returned from command ";
            errorMessage += command + "on path ";
            errorMessage += directory.getPath() + " :\n" + convertToString(p.getErrorStream());
            throw new RuntimeException(errorMessage);
        }
    }


    private static String addQuote(String original) {
        return "\"" + original + "\"";
    }

    private static boolean isWindows() {
        return (System.getProperty("os.name").toLowerCase().indexOf("win") >= 0);
    }

    /**
     * Converts the {@code is} into a {@code String} and returns it.
     */
    private static String convertToString(InputStream is) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            int len;
            while ((len = is.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return baos.toString();
    }
}
