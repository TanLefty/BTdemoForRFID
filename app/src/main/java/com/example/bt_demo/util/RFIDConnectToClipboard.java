package com.example.bt_demo.util;

import android.util.Log;
import android.content.Context;
import android.widget.Toast;

import java.util.ArrayList;


/**
 * Created by Administrator on 2017/8/4 0004.
 */

public class RFIDConnectToClipboard {
    public static String Connect_CMD_StartScan = "#STARTSCAN#";
    public static String Connect_CMD_StopScan = "#STOPSCAN#";
    public static String Connect_CMD_GetRFID = "#GETRFID#";
    public static String Connect_CMD_GetRFIDEnd = "#GETRFIDEND#";
    public static String Connect_CMD_GetFirstRFID = "#GETFIRSTRFID#";
    public static String Connect_CMD_GetFirstRFIDEnd = "#GETFIRSTRFIDEND#";
    public static String Connect_CMD_Reset = "#RESET#";
    public static String Connect_CMD_ResetEnd = "#RESETEND#";
    private static int RFID_Data_Length = 38;
    public ArrayList<String> RFID_Result;
    public boolean Scan_Status = false;
    public boolean Read_Last_RFID = false;


    public void init(){
        RFID_Result = new ArrayList<String>();
    }

    //读取到的RFID数据进行解码
    public String of_decode(String readRFIDData){
        String str_ag_Str = "";
        //String str_Tex_Result = my_Text.getText().toString();
        String str_Ret_Result = "";
        String str_RFID = "";
        int li_RFIDCount = 0;
        int li_FindBeginIndex = -1;
        int li_FindEndIndex = -1;
        int li_ModResult = 0;

        str_ag_Str = readRFIDData;
        //确保数据完整性
        do {
            //替换空白,数据头,数据结尾
            str_ag_Str = str_ag_Str.replaceAll(" ", "");
            str_ag_Str = str_ag_Str.replaceAll("AA03100155", "");//Begin
            str_ag_Str = str_ag_Str.replaceAll("AA03120055", "");//End
            //判断总长度是否大于/等于38
            if(str_ag_Str.length() < RFID_Data_Length){
                break;
            }
            //判断总长度是否成立
            li_ModResult = str_ag_Str.length() % RFID_Data_Length;
            if(li_ModResult != 0){
                if(!str_ag_Str.substring(0, 2).equals("AA")){
                    if(str_ag_Str.substring(li_ModResult, li_ModResult + 2).equals("AA")){
                        str_ag_Str = str_ag_Str.substring(li_ModResult);
                    }else{
                        li_FindBeginIndex = str_ag_Str.indexOf("55AA");
                        if(li_FindBeginIndex != -1){
                            li_FindBeginIndex += 2;
                            str_ag_Str = str_ag_Str.substring(li_FindBeginIndex);
                        }
                    }
                }

                if(!str_ag_Str.substring(str_ag_Str.length() - 2).equals("55")){
                    if(str_ag_Str.substring(str_ag_Str.length() - li_ModResult - 2, str_ag_Str.length() - li_ModResult).equals("55")){
                        str_ag_Str = str_ag_Str.substring(0, str_ag_Str.length() - li_ModResult);
                    }else{
                        li_FindEndIndex = str_ag_Str.lastIndexOf("55");
                        if(li_FindEndIndex != -1){
                            str_ag_Str = str_ag_Str.substring(0,li_FindEndIndex + 2);
                        }
                    }
                }
            }
        }while(!str_ag_Str.substring(0, 2).equals("AA") && !str_ag_Str.substring(str_ag_Str.length() - 2).equals("55"));

        //解码数据,分解RFID
        while(str_ag_Str.length() >= RFID_Data_Length){
            //找到数据头分隔符
            if(str_ag_Str.substring(0, 2).equals("AA")){
                li_FindBeginIndex = 0;
            }else{
                li_FindBeginIndex = str_ag_Str.indexOf("AA");
                str_ag_Str = str_ag_Str.substring(li_FindBeginIndex, str_ag_Str.length() - li_FindBeginIndex);
                li_FindBeginIndex = 0;
            }

            //找到数据结尾分隔符
            if(str_ag_Str.length() >= RFID_Data_Length && str_ag_Str.substring(36, RFID_Data_Length).equals("55")){
                li_FindEndIndex = RFID_Data_Length;
            }

            //取截断数据，取中间数据
            str_RFID = str_ag_Str.substring(li_FindBeginIndex, li_FindEndIndex);
            str_RFID = str_ag_Str.substring(12, 36);
            //判断成立则输出结果
            if(RFID_Result.indexOf(str_RFID) == -1){
            //str_Tex_Result.indexOf(str_RFID) == -1 && str_Ret_Result.indexOf(str_RFID) == -1){
                //插入新结果
                RFID_Result.add(str_RFID);
                str_RFID = "#" + str_RFID + "#\n";
                str_Ret_Result = str_Ret_Result.concat(str_RFID);   //concat拼接字符串方法
                li_RFIDCount += 1;
                Log.d("message", "str_Ret_Result"+ String.valueOf(li_RFIDCount) +" is: " + str_Ret_Result);
            }
            //截断数据继续查找RFID
            if(str_ag_Str.length() / RFID_Data_Length > 1){
                str_ag_Str = str_ag_Str.substring(RFID_Data_Length, str_ag_Str.length() - RFID_Data_Length);
            }else{
                str_ag_Str = "";
            }
        };
        return str_Ret_Result;
    }

    //定时指令执行器
    public String of_timer_cmd(Context context, String connectCMD){
        String retResult = "";


        do {
            if(Scan_Status == false && RFID_Result.size() > 0){
                //开始扫描RFID
                //发送通知APP指令, 返回开始扫描RFID指令
                //ClipboardUtil.getInstance().copyText("TEXT", Connect_CMD_StartScan);
                retResult = Connect_CMD_StartScan;
                Scan_Status = true;
                //Toast.makeText(context.getApplicationContext(), "扫描RFID开始，请在RFID标签盘点界面【启动】接收!", Toast.LENGTH_SHORT).show();

                break;
            } else if(Scan_Status == true && RFID_Result.size() == 0 && Read_Last_RFID == true){
                //开始扫描RFID
                //发送通知APP指令, 返回结束扫描RFID指令
                //ClipboardUtil.getInstance().copyText("TEXT", Connect_CMD_StopScan);
                retResult = Connect_CMD_StopScan;
                Scan_Status = false;
                Toast.makeText(context.getApplicationContext(), "扫描RFID结果已传输完毕!", Toast.LENGTH_SHORT).show();
                break;
            }

            //
            if (connectCMD.equals(Connect_CMD_Reset)) {
                //开始清除RFID结果集
                //执行指令,返回执行结束指令
                RFID_Result.clear();
                //ClipboardUtil.getInstance().copyText("TEXT", Connect_CMD_ResetEnd);
                retResult = Connect_CMD_ResetEnd;
                Read_Last_RFID = true;
            } else if (connectCMD.equals(Connect_CMD_GetRFID)) {
                //收到读取RFID 指令
                //读取成功,返回RFID读取结束指令 + RFID结果
                if (RFID_Result.size() == 0) {
                    //ClipboardUtil.getInstance().copyText("TEXT", Connect_CMD_GetRFIDEnd + RFID_Result.get(0));
                    Read_Last_RFID = true;
                }else{
                    retResult = Connect_CMD_GetRFIDEnd + RFID_Result.get(0);
                    RFID_Result.remove(0);
                    Read_Last_RFID = false;
                }


            } else if (connectCMD.equals(Connect_CMD_GetFirstRFID)) {
                //收到读取第一个RFID指令
                //读取成功,返回RFID读取结束指令 + RFID结果
                if (RFID_Result.size() == 0) {
                    //ClipboardUtil.getInstance().copyText("TEXT", Connect_CMD_GetRFIDEnd + RFID_Result.get(0));
                    Read_Last_RFID = true;
                }else{
                    retResult = Connect_CMD_GetFirstRFIDEnd + RFID_Result.get(0);
                    RFID_Result.remove(0);
                    Read_Last_RFID = false;
                }
            }
        }while(false);
        return retResult;
    }



//    public String of_getClipboardData(){
//        /**
//         *  myClipboard.hasPrimaryClip()判断是否存在copy都值，我们把null传给了剪贴板。
//         *      所有是有个null值都
//         *
//         *      当第一个条件为false当时候，就不会执行第二条
//         */
//        String retResult = "";
//        ClipData.Item item = null;
//        if (nClipboardManager.hasPrimaryClip() && nClipboardManager.equals(null)) {
//            //copy内容是null值，停止粘贴。
//            nClipData = nClipboardManager.getPrimaryClip();
//            item = nClipData.getItemAt(0);
//            retResult = item.toString();
//            //检测到null值了，把copy到第二个值传入剪贴板。
//            //Toast.makeText(getApplicationContext(), "存在null值,不粘贴", Toast.LENGTH_SHORT).show();
//        } else {
//            //copy内容不等于null值，需要粘贴
//            //获取copy都内容
//            nClipData = nClipboardManager.getPrimaryClip();
//            item = nClipData.getItemAt(0);
//            retResult = item.toString();
//            ///Toast.makeText(getApplicationContext(), "粘贴成功", Toast.LENGTH_SHORT).show();
//        }
//        return retResult;
//    }
//
//    public void  of_setClipboardData(String copyData){
//        nClipData = ClipData.newPlainText("Text", copyData);
//        nClipboardManager.setPrimaryClip(nClipData);
//    }
}
