package fileCopying;

import java.io.*;

public class MultithreadedCopy {
    public static void main(String[] args) {
        int thread_num = 5;//创建5个线程读写
        String srcFileStr = "C:\\Users\\asus\\Desktop\\idea.txt";
        String desFileStr = "C:\\Users\\asus\\Desktop\\idea2.txt";
        File srcFile = new File(srcFileStr);
        long workload = srcFile.length()/thread_num;//总共要读/写多少个字节
        //用一个数组来存储每个线程跳过的字节数
        long[] skipLenArr = new long[thread_num];
        for(int i = 0;i<skipLenArr.length;i++){
            skipLenArr[i] = i*workload;
        }
        //用一个数组来存储所有的线程
        ThreadFileCopy[] tfcs = new ThreadFileCopy[thread_num];
        //初始化所有线程
        for(int i = 0;i<tfcs.length;i++){
            tfcs[i] = new ThreadFileCopy(srcFileStr,desFileStr,skipLenArr[i],workload);
        }
        //让所有线程进入就绪状态
        for(int i = 0;i<tfcs.length;i++){
            tfcs[i].start();
        }
        System.out.println("复制完毕！");
    }
}


class ThreadFileCopy extends Thread {
    private String srcFileStr;//源文件的路径
    private String desFileStr;//目标文件的路径 ,des --> destination目的地
    private long skipLen;//跳过多少个字节开始读/写
    private long workload;//总共要读/写多少个字节
    private final int IO_UNIT = 1024;//每次读写的基本单位（1024个字节）

    public ThreadFileCopy(String srcFileStr, String desFileStr, long skipLen, long workload) {
        this.srcFileStr = srcFileStr;
        this.desFileStr = desFileStr;
        this.skipLen = skipLen;
        this.workload = workload;
    }
    public void run(){
        FileInputStream fis = null;
        BufferedInputStream bis = null;//利用高级缓冲流，加快读的速度
        RandomAccessFile raf = null;
        try {
            fis = new FileInputStream(srcFileStr);
            bis = new BufferedInputStream(fis);
            raf = new RandomAccessFile(desFileStr,"rw");
            bis.skip(this.skipLen);//跳过一部分字节开始读
            raf.seek(this.skipLen);//跳过一部分字节开始写
            byte[] bytes = new byte[IO_UNIT];
            //根据总共需要复制的字节数 和 读写的基本单元 计算出一共需要读写的次数，利用读写次数控制循环
            long io_num = this.workload/IO_UNIT + 1;//因为workload/1024 很可能不能整除，会有余数
            if(this.workload % IO_UNIT == 0)
                io_num--;//如果碰巧整除，读写次数减一
            //count表示读取的有效字节数，虽然count不参与控制循环结束，
            // 但是它能有效避免最后一次读取出的byte数组中有大量空字节写入到文件中，导致复制出的文件稍稍变大
            int count = bis.read(bytes);
            while (io_num != 0){
                raf.write(bytes,0,count);
                count = bis.read(bytes,0,count);//重新计算count的值
                io_num--;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if (fis != null)
                    fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (bis != null)
                    bis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (raf != null)
                    raf.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
