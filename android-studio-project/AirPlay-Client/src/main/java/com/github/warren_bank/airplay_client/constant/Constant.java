package com.github.warren_bank.airplay_client.constant;

public class Constant {
    public interface Msg {
        public static final int Msg_AirPlay_Show_Connect_Dialog  = 1;
        public static final int Msg_AirPlay_Connect              = 2;
        public static final int Msg_AirPlay_Disconnect           = 3;

        public static final int Msg_Photo                        = 4;
        public static final int Msg_Play                         = 5;
        public static final int Msg_Stop                         = 6;

        public static final int Msg_ScreenMirror_Stream_Start    = 7;
        public static final int Msg_ScreenMirror_Stream_Pause    = 8;
        public static final int Msg_ScreenMirror_Stream_Resume   = 9;
        public static final int Msg_ScreenMirror_Stream_Stop     = 10;
        public static final int Msg_ScreenMirror_Detect_Rotation = 11;

        public static final int Msg_Change_Folder_Layout         = 12;
        public static final int Msg_Restart_Http_Server          = 13;
        public static final int Msg_Exit_Service                 = 14;
    }

    public interface Target {
        public static final String PHOTO                         = "/photo";
        public static final String STOP                          = "/stop";
        public static final String PLAY                          = "/play";
    }

    public interface PermissionRequestCode {
        public static final int POST_NOTIFICATIONS              = 1;
        public static final int READ_EXTERNAL_STORAGE           = 2;
        public static final int MANAGE_EXTERNAL_STORAGE         = 3;
        public static final int SCREEN_CAPTURE                  = 4;
        public static final int SETTINGS                        = 5;
    }

}
