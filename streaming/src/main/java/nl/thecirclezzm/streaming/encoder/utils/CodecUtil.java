package nl.thecirclezzm.streaming.encoder.utils;

import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by pedro on 14/02/18.
 */

public class CodecUtil {

    public static final String H264_MIME = "video/avc";
    public static final String H265_MIME = "video/hevc";
    public static final String AAC_MIME = "audio/mp4a-latm";
    private static final String TAG = "CodecUtil";

    @NonNull
    @RequiresApi(api = Build.VERSION_CODES.M)
    public static List<String> showAllCodecsInfo() {
        List<MediaCodecInfo> mediaCodecInfoList = getAllCodecs();
        List<String> infos = new ArrayList<>();
        for (MediaCodecInfo mediaCodecInfo : mediaCodecInfoList) {
            StringBuilder info = new StringBuilder("----------------\n");
            info.append("Name: ").append(mediaCodecInfo.getName()).append("\n");
            for (String type : mediaCodecInfo.getSupportedTypes()) {
                info.append("Type: ").append(type).append("\n");
                MediaCodecInfo.CodecCapabilities codecCapabilities =
                        mediaCodecInfo.getCapabilitiesForType(type);
                info.append("Max instances: ").append(codecCapabilities.getMaxSupportedInstances()).append("\n");
                if (mediaCodecInfo.isEncoder()) {
                    info.append("----- Encoder info -----\n");
                    MediaCodecInfo.EncoderCapabilities encoderCapabilities =
                            codecCapabilities.getEncoderCapabilities();
                    info.append("Complexity range: ").append(encoderCapabilities.getComplexityRange().getLower()).append(" - ").append(encoderCapabilities.getComplexityRange().getUpper()).append("\n");
                    info.append("Quality range: ").append(encoderCapabilities.getQualityRange().getLower()).append(" - ").append(encoderCapabilities.getQualityRange().getUpper()).append("\n");
                    info.append("CBR supported: ").append(encoderCapabilities.isBitrateModeSupported(MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_CBR)).append("\n");
                    info.append("VBR supported: ").append(encoderCapabilities.isBitrateModeSupported(MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_VBR)).append("\n");
                    info.append("CQ supported: ").append(encoderCapabilities.isBitrateModeSupported(MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_CQ)).append("\n");
                    info.append("----- -----\n");
                } else {
                    info.append("----- Decoder info -----\n");
                    info.append("----- -----\n");
                }

                if (codecCapabilities.colorFormats != null && codecCapabilities.colorFormats.length > 0) {
                    info.append("----- Video info -----\n");
                    info.append("Supported colors: \n");
                    for (int color : codecCapabilities.colorFormats)
                        info.append(color).append("\n");
                    for (MediaCodecInfo.CodecProfileLevel profile : codecCapabilities.profileLevels)
                        info.append("Profile: ").append(profile.profile).append(", level: ").append(profile.level).append("\n");
                    MediaCodecInfo.VideoCapabilities videoCapabilities =
                            codecCapabilities.getVideoCapabilities();

                    info.append("Bitrate range: ").append(videoCapabilities.getBitrateRange().getLower()).append(" - ").append(videoCapabilities.getBitrateRange().getUpper()).append("\n");
                    info.append("Frame rate range: ").append(videoCapabilities.getSupportedFrameRates().getLower()).append(" - ").append(videoCapabilities.getSupportedFrameRates().getUpper()).append("\n");
                    info.append("Width range: ").append(videoCapabilities.getSupportedWidths().getLower()).append(" - ").append(videoCapabilities.getSupportedWidths().getUpper()).append("\n");
                    info.append("Height range: ").append(videoCapabilities.getSupportedHeights().getLower()).append(" - ").append(videoCapabilities.getSupportedHeights().getUpper()).append("\n");
                    info.append("----- -----\n");
                } else {
                    info.append("----- Audio info -----\n");
                    for (MediaCodecInfo.CodecProfileLevel profile : codecCapabilities.profileLevels)
                        info.append("Profile: ").append(profile.profile).append(", level: ").append(profile.level).append("\n");
                    MediaCodecInfo.AudioCapabilities audioCapabilities =
                            codecCapabilities.getAudioCapabilities();

                    info.append("Bitrate range: ").append(audioCapabilities.getBitrateRange().getLower()).append(" - ").append(audioCapabilities.getBitrateRange().getUpper()).append("\n");
                    info.append("Channels supported: ").append(audioCapabilities.getMaxInputChannelCount()).append("\n");
                    try {
                        if (audioCapabilities.getSupportedSampleRates() != null
                                && audioCapabilities.getSupportedSampleRates().length > 0) {
                            info.append("Supported sample rate: \n");
                            for (int sr : audioCapabilities.getSupportedSampleRates())
                                info.append(sr).append("\n");
                        }
                    } catch (Exception ignored) {
                    }
                    info.append("----- -----\n");
                }
                info.append("Max instances: ").append(codecCapabilities.getMaxSupportedInstances()).append("\n");
            }
            info.append("----------------\n");
            infos.add(info.toString());
        }
        return infos;
    }

    @NonNull
    public static List<MediaCodecInfo> getAllCodecs() {
        List<MediaCodecInfo> mediaCodecInfoList = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= 21) {
            MediaCodecList mediaCodecList = new MediaCodecList(MediaCodecList.ALL_CODECS);
            MediaCodecInfo[] mediaCodecInfos = mediaCodecList.getCodecInfos();
            mediaCodecInfoList.addAll(Arrays.asList(mediaCodecInfos));
        } else {
            int count = MediaCodecList.getCodecCount();
            for (int i = 0; i < count; i++) {
                MediaCodecInfo mci = MediaCodecList.getCodecInfoAt(i);
                mediaCodecInfoList.add(mci);
            }
        }
        return mediaCodecInfoList;
    }

    @NonNull
    public static List<MediaCodecInfo> getAllHardwareEncoders(String mime) {
        List<MediaCodecInfo> mediaCodecInfoList = getAllEncoders(mime);
        List<MediaCodecInfo> mediaCodecInfoHardware = new ArrayList<>();
        for (MediaCodecInfo mediaCodecInfo : mediaCodecInfoList) {
            String name = mediaCodecInfo.getName().toLowerCase();
            if (!name.contains("omx.google") && !name.contains("sw")) {
                mediaCodecInfoHardware.add(mediaCodecInfo);
            }
        }
        return mediaCodecInfoHardware;
    }

    @NonNull
    public static List<MediaCodecInfo> getAllHardwareDecoders(String mime) {
        List<MediaCodecInfo> mediaCodecInfoList = getAllDecoders(mime);
        List<MediaCodecInfo> mediaCodecInfoHardware = new ArrayList<>();
        for (MediaCodecInfo mediaCodecInfo : mediaCodecInfoList) {
            String name = mediaCodecInfo.getName().toLowerCase();
            if (!name.contains("omx.google") && !name.contains("sw")) {
                mediaCodecInfoHardware.add(mediaCodecInfo);
            }
        }
        return mediaCodecInfoHardware;
    }

    @NonNull
    public static List<MediaCodecInfo> getAllSoftwareEncoders(String mime) {
        List<MediaCodecInfo> mediaCodecInfoList = getAllEncoders(mime);
        List<MediaCodecInfo> mediaCodecInfoSoftware = new ArrayList<>();
        for (MediaCodecInfo mediaCodecInfo : mediaCodecInfoList) {
            String name = mediaCodecInfo.getName().toLowerCase();
            if (name.contains("omx.google") || name.contains("sw")) {
                mediaCodecInfoSoftware.add(mediaCodecInfo);
            }
        }
        return mediaCodecInfoSoftware;
    }

    @NonNull
    public static List<MediaCodecInfo> getAllSoftwareDecoders(String mime) {
        List<MediaCodecInfo> mediaCodecInfoList = getAllDecoders(mime);
        List<MediaCodecInfo> mediaCodecInfoSoftware = new ArrayList<>();
        for (MediaCodecInfo mediaCodecInfo : mediaCodecInfoList) {
            String name = mediaCodecInfo.getName().toLowerCase();
            if (name.contains("omx.google") || name.contains("sw")) {
                mediaCodecInfoSoftware.add(mediaCodecInfo);
            }
        }
        return mediaCodecInfoSoftware;
    }

    @NonNull
    public static List<MediaCodecInfo> getAllEncoders(String mime) {
        if (Build.VERSION.SDK_INT >= 21) {
            return getAllEncodersAPI21(mime);
        } else {
            return getAllEncodersAPI16(mime);
        }
    }

    @NonNull
    public static List<MediaCodecInfo> getAllDecoders(String mime) {
        if (Build.VERSION.SDK_INT >= 21) {
            return getAllDecodersAPI21(mime);
        } else {
            return getAllDecodersAPI16(mime);
        }
    }

    /**
     * choose the video encoder by mime. API 21+
     */
    @NonNull
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private static List<MediaCodecInfo> getAllEncodersAPI21(String mime) {
        List<MediaCodecInfo> mediaCodecInfoList = new ArrayList<>();
        MediaCodecList mediaCodecList = new MediaCodecList(MediaCodecList.ALL_CODECS);
        MediaCodecInfo[] mediaCodecInfos = mediaCodecList.getCodecInfos();
        for (MediaCodecInfo mci : mediaCodecInfos) {
            if (!mci.isEncoder()) {
                continue;
            }
            String[] types = mci.getSupportedTypes();
            for (String type : types) {
                if (type.equalsIgnoreCase(mime)) {
                    mediaCodecInfoList.add(mci);
                }
            }
        }
        return mediaCodecInfoList;
    }

    /**
     * choose the video encoder by mime. API > 16
     */
    @NonNull
    private static List<MediaCodecInfo> getAllEncodersAPI16(String mime) {
        List<MediaCodecInfo> mediaCodecInfoList = new ArrayList<>();
        int count = MediaCodecList.getCodecCount();
        for (int i = 0; i < count; i++) {
            MediaCodecInfo mci = MediaCodecList.getCodecInfoAt(i);
            if (!mci.isEncoder()) {
                continue;
            }
            String[] types = mci.getSupportedTypes();
            for (String type : types) {
                if (type.equalsIgnoreCase(mime)) {
                    mediaCodecInfoList.add(mci);
                }
            }
        }
        return mediaCodecInfoList;
    }

    /**
     * choose the video encoder by mime. API 21+
     */
    @NonNull
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private static List<MediaCodecInfo> getAllDecodersAPI21(String mime) {
        List<MediaCodecInfo> mediaCodecInfoList = new ArrayList<>();
        MediaCodecList mediaCodecList = new MediaCodecList(MediaCodecList.ALL_CODECS);
        MediaCodecInfo[] mediaCodecInfos = mediaCodecList.getCodecInfos();
        for (MediaCodecInfo mci : mediaCodecInfos) {
            if (mci.isEncoder()) {
                continue;
            }
            String[] types = mci.getSupportedTypes();
            for (String type : types) {
                if (type.equalsIgnoreCase(mime)) {
                    mediaCodecInfoList.add(mci);
                }
            }
        }
        return mediaCodecInfoList;
    }

    /**
     * choose the video encoder by mime. API > 16
     */
    @NonNull
    private static List<MediaCodecInfo> getAllDecodersAPI16(String mime) {
        List<MediaCodecInfo> mediaCodecInfoList = new ArrayList<>();
        int count = MediaCodecList.getCodecCount();
        for (int i = 0; i < count; i++) {
            MediaCodecInfo mci = MediaCodecList.getCodecInfoAt(i);
            if (mci.isEncoder()) {
                continue;
            }
            String[] types = mci.getSupportedTypes();
            for (String type : types) {
                if (type.equalsIgnoreCase(mime)) {
                    mediaCodecInfoList.add(mci);
                }
            }
        }
        return mediaCodecInfoList;
    }

    public enum Force {
        FIRST_COMPATIBLE_FOUND, SOFTWARE, HARDWARE
    }
}
