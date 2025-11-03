package com.example.sftpuploader.util;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.ChannelSftp.LsEntry;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * Small CLI utility to list a remote SFTP directory in an "ls -ltar" like format.
 * Usage (example):
 * mvn exec:java -Dexec.mainClass="com.example.sftpuploader.util.RemoteDirLister" -Dexec.args="host user password /upload"
 *
 * If you want me to run it here, provide SFTP credentials and the remote path.
 */
public class RemoteDirLister {
    public static void main(String[] args) throws Exception {
        if (args.length < 4) {
            System.err.println("Usage: RemoteDirLister <host> <username> <password> <remoteDir>");
            System.exit(2);
        }

        String host = args[0];
        int port = 22;
        String username = args[1];
        String password = args[2];
        String remoteDir = args[3];

        JSch jsch = new JSch();
        Session session = null;
        ChannelSftp channel = null;

        try {
            session = jsch.getSession(username, host, port);
            session.setPassword(password);
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect(10000);

            channel = (ChannelSftp) session.openChannel("sftp");
            channel.connect(10000);

            @SuppressWarnings("unchecked")
            List<LsEntry> entries = new ArrayList<>((List<LsEntry>) channel.ls(remoteDir));

            // Include hidden (ls -a) already included by channel.ls
            // sort by mtime (ls -t) then reverse (ls -r)
            entries.sort(Comparator.comparingInt((LsEntry e) -> e.getAttrs().getMTime())); // oldest first

            SimpleDateFormat df = new SimpleDateFormat("MMM dd HH:mm");

            for (LsEntry e : entries) {
                SftpATTRS a = e.getAttrs();
                String perms = formatPermissions(a);
                int mtime = a.getMTime();
                String date = df.format(new Date(((long) mtime) * 1000L));
                String name = e.getFilename();
                // Skip '.' and '..' to mimic typical ls -la behavior
                if (".".equals(name) || "..".equals(name)) continue;
                System.out.printf("%s %3d %5d %5d %8d %s %s\n",
                        perms,
                        1,
                        a.getUId(),
                        a.getGId(),
                        a.getSize(),
                        date,
                        name);
            }

        } finally {
            if (channel != null && channel.isConnected()) channel.disconnect();
            if (session != null && session.isConnected()) session.disconnect();
        }
    }

    private static String formatPermissions(SftpATTRS a) {
        StringBuilder sb = new StringBuilder();
        sb.append(a.isDir() ? 'd' : '-');
        int perm = a.getPermissions();
        // owner (0400,0200,0100)
        sb.append((perm & 0400) != 0 ? 'r' : '-');
        sb.append((perm & 0200) != 0 ? 'w' : '-');
        sb.append((perm & 0100) != 0 ? 'x' : '-');
        // group (0040,0020,0010)
        sb.append((perm & 0040) != 0 ? 'r' : '-');
        sb.append((perm & 0020) != 0 ? 'w' : '-');
        sb.append((perm & 0010) != 0 ? 'x' : '-');
        // others (0004,0002,0001)
        sb.append((perm & 0004) != 0 ? 'r' : '-');
        sb.append((perm & 0002) != 0 ? 'w' : '-');
        sb.append((perm & 0001) != 0 ? 'x' : '-');
        return sb.toString();
    }
}
