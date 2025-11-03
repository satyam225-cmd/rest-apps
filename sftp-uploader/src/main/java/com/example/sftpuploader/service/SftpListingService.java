package com.example.sftpuploader.service;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Service
public class SftpListingService {

    public List<Map<String, Object>> listDirectory(String host, int port, String username, String password, String remoteDir) throws Exception {
        JSch jsch = new JSch();
        Session session = null;
        ChannelSftp channel = null;
        List<Map<String, Object>> result = new ArrayList<>();

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

            // sort by mtime ascending (oldest first). To mimic ls -ltar you can reverse etc.
            entries.sort(Comparator.comparingInt((LsEntry e) -> e.getAttrs().getMTime()));

            SimpleDateFormat df = new SimpleDateFormat("MMM dd HH:mm");

            for (LsEntry e : entries) {
                String name = e.getFilename();
                if (".".equals(name) || "..".equals(name)) continue;
                SftpATTRS a = e.getAttrs();
                int mtime = a.getMTime();

                Map<String, Object> m = new HashMap<>();
                m.put("perms", formatPermissions(a));
                m.put("uid", a.getUId());
                m.put("gid", a.getGId());
                m.put("size", a.getSize());
                m.put("date", df.format(new Date(((long) mtime) * 1000L)));
                m.put("name", name);
                result.add(m);
            }

            return result;
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
