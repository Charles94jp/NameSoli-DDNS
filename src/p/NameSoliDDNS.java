package p;

import javax.net.ssl.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.RSAOtherPrimeInfo;

import Toolbox.ProgressBar;
import Toolbox.NowString;

public class NameSoliDDNS {

    private static final int IS_TEST = 0; // 测试为1，不在测试为0，用于代理

    /**
     * 程序入口
     *
     * @param args args[0]为密钥，args[1]为域名，不带前缀
     */
    public static void main(String[] args) {

        if (args.length == 2) {
            DNS_KEY = args[0];
            DNS_DOMAIN = args[1];
        } else if (args.length == 3) {
            DNS_KEY = args[0];
            DNS_DOMAIN = args[1];
            FREQUENCY = Long.valueOf(args[2]);
        } else {
            System.out.println("The program requires at least two parameters\n" +
                    "1: namesilo key; 2: domain name (without prefix); optional parameter 3: the frequency of querying the local IP address (in milliseconds), the default is 600,000 milliseconds(10 min) once");
            System.exit(-1);
        }


        // 测试用
        if (IS_TEST != 0) {
            System.setProperty("http.proxyHost", "localhost");
            System.setProperty("http.proxyPort", "8080");
            System.setProperty("https.proxyHost", "localhost");
            System.setProperty("https.proxyPort", "8080");
        }

        restart();
    }

    private static String DNS_KEY;
    private static String DNS_DOMAIN;
    private static long FREQUENCY = 600000;

    private static String DNS_IP;
    private static String DNS_RRID;

    private static final String GET_IP_URL = "https://202020.ip138.com/";
    private static final String[][] GET_IP_HEAD = {
            {"Host", "202020.ip138.com"},
            {"User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:82.0) Gecko/20100101 Firefox/82.0"},
            {"Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8"},
            {"Accept-Language", "zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2"},
            {"Accept-Encoding", "gzip, deflate"},
            {"Connection", "close"},
            {"Referer", "https://www.ip138.com/"},
            {"Upgrade-Insecure-Requests", "1"}
    };

    private static final String API_GET_DNS_LIST_URL = "https://www.namesilo.com/api/dnsListRecords?version=1&type=xml";
    private static final String API_UPDATE_DNS_URL = "https://www.namesilo.com/api/dnsUpdateRecord?version=1&type=xml&rrttl=7207&rrhost="; // rrhost即前缀，这里更新的是无前缀部分，rrid这里没有给出,rrvalue即新的IP
    private static final String[][] NAMESILO_HEAD = {
            {"Host", "www.namesilo.com"},
            //{"User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:82.0) Gecko/20100101 Firefox/82.0"},
            //{"Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8"},
            //{"Accept-Language", "zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2"},
            //{"Accept-Encoding", "gzip, deflate"},
            {"Content-Type", "application/x-www-form-urlencoded;charset=UTF-8"},
            {"Connection", "close"},
            {"Upgrade-Insecure-Requests", "1"}
    };

    /**
     * apiGetIP();
     */
    public static void init() {
        if (apiGetIP() == -1) {
            System.out.println("init failed: Please check [ your namesilo key ] / [ domain name (without prefix) ] or [ your network ]");
            System.exit(-1);
        }
    }

    /**
     * @return 访问https://202020.ip138.com/获取到的 IP
     */
    public static String getMyIP() {
        String response = doGet(GET_IP_URL, GET_IP_HEAD);
        int a = response.indexOf("是：");
        int z = response.indexOf("</title>");
        if (a == -1 || z == -1) {
            System.out.println("getMyIP(): " + response);
            System.exit(-1);
        }
        String myip = response.substring(a + 2, z);
        NowString.printNow();
        System.out.println(" Get IP from 202020.ip138.com: " + myip);

        return myip;
    }

    /**
     * update DNS_IP & DNS_RRID
     *
     * @return 0 on success;
     */
    public static int apiGetIP() {
        String response = doGet(API_GET_DNS_LIST_URL + "&key=" + DNS_KEY + "&domain=" + DNS_DOMAIN, NAMESILO_HEAD);
        int a = response.indexOf("<host>" + DNS_DOMAIN + "</host><value>");
        if (a == -1) {
            System.out.println("apiGetIP(): " + response);
            return -1;
        }
        int z = response.indexOf("</value>", a);
        DNS_IP = response.substring(a + 32, z);
        System.out.println("Get IP from NameSilo: " + DNS_IP);

        a = response.lastIndexOf("<record_id>", a);
        z = response.indexOf("</record_id>", a);
        DNS_RRID = response.substring(a + 11, z);

        return 0;
    }

    /**
     * 循环地检查IP是否有变动，有则提交
     *
     * @return
     */
    public static int loop() {
        ProgressBar bar = new ProgressBar();
        String myip = null;
        while (true) {
            myip = getMyIP();
            if (!myip.equals(DNS_IP)) {
                if (apiUpdate(myip) == -1) {
                    // github操作
                    return -1;
                }
            }

            // 暂停10分钟，打印进度条
            try {
                //bar.noBarPrint(15000);
                bar.noBarPrint(FREQUENCY);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 更新DNS上的IP，更新 DNSDNS_RRID
     *
     * @param myip 要更新的IP
     * @return 不成功返回-1，其他值都是成功
     */
    public static int apiUpdate(String myip) {
        String response = doGet(API_UPDATE_DNS_URL + "&rrvalue=" + myip + "&rrid=" + DNS_RRID + "&key=" + DNS_KEY + "&domain=" + DNS_DOMAIN, NAMESILO_HEAD);
        int a = response.lastIndexOf("<record_id>");
        if (a == -1) {
            System.out.println(response);
            return -1;
        }
        int z = response.indexOf("</record_id>", a);
        DNS_RRID = response.substring(a + 11, z);
        a = response.indexOf("</operation><ip>");
        z = response.indexOf("</ip>", a);
        DNS_IP = response.substring(a + 16, z);
        //DNS_IP=
        if (response.indexOf("<code>300</code><detail>success</detail>") != -1) {
            System.out.println("apiUpdate completed");
            return 0;
        }
        return -1;
    }

    /**
     * init();loop();
     */
    public static void restart() {
        init();
        System.exit(loop());
    }


    /***************************************** 发送 get请求 *******************************************************/

    /**
     * @param URL
     * @param head
     * @return
     * @link https://www.jianshu.com/p/117264481886
     */
    public static String doGet(String URL, String[][] head) {
        HttpURLConnection conn = null;
        InputStream is = null;
        BufferedReader br = null;
        StringBuilder result = new StringBuilder();
        try {
            //创建远程url连接对象
            URL url = new URL(URL);

            if (IS_TEST != 0) {
                trustAllHosts();
            }
            //通过远程url连接对象打开一个连接，强转成HTTPURLConnection类
            HttpsURLConnection https = (HttpsURLConnection) url.openConnection();

            if (IS_TEST != 0) {
                https.setHostnameVerifier(DO_NOT_VERIFY);
            }

            conn = https;
            conn.setRequestMethod("GET");
            //设置连接超时时间和读取超时时间
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(50000);
            for (int i = 1; i < head.length; ++i) {
                conn.setRequestProperty(head[i][0], head[i][1]);
            }
            //发送请求
            conn.connect();
            //通过conn取得输入流，并使用Reader读取
            if (200 == conn.getResponseCode()) {
                is = conn.getInputStream();
                br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                String line;
                while ((line = br.readLine()) != null) {
                    result.append(line);
                    //System.out.println(line);
                }
            } else {
                System.out.println("ResponseCode is an error code:" + conn.getResponseCode());
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            // 网站超时未返回
            if (e.toString().equals("java.net.SocketTimeoutException: Read timed out")) {
                // 先关闭当前的连接资源
                try {
                    if (br != null) {
                        br.close();
                    }
                    if (is != null) {
                        is.close();
                    }
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
                conn.disconnect();

                restart();
            }
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
                if (is != null) {
                    is.close();
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
            conn.disconnect();
        }
        return result.toString();
    }

    /***************************************** 代理时信任 HTTPS 证书 *******************************************************/


    /**
     * @link https://blog.csdn.net/chaishen10000/article/details/82992291
     */
    final static HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };

    /**
     * Trust every server - dont check for any certificate
     */
    private static void trustAllHosts() {
        final String TAG = "trustAllHosts";
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new java.security.cert.X509Certificate[]{};
            }

            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                //Log.i(TAG, "checkClientTrusted");
            }

            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                //Log.i(TAG, "checkServerTrusted");
            }
        }};
        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
