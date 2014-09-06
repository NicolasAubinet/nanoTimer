package com.cube.nanotimer.util.stackmat;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

public class StackmatController {

//  private MediaRecorder recorder = new MediaRecorder();

//  private static final String FILENAME = Environment.getExternalStorageDirectory().getAbsolutePath() + "/stackmatoutput.out";

  private AudioRecord audioRecord;
//  private int minbuffersize;
//  public LinkedList<byte[]> dataList = new LinkedList<byte[]>();
  public boolean stop = false;

//  private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
  private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
//  private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_8BIT;
  private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

  private final Object listSync = new Object();

  public StackmatController() {
  }

  public void startRecording() {
    new Thread() {
      @Override
      public void run() {
        executeStartRecording();
      }
    }.start();
  }

  private void executeStartRecording() {
    Log.i("[StackmatController]", "Record--------------" + Thread.currentThread().getId());
    int rate = 8000;
    /*for (int r : new int[] { 1200, 8000, 11025, 16000, 22050, 44100, 48000 }) {
      for (int f : new int[] { AudioFormat.ENCODING_PCM_8BIT, AudioFormat.ENCODING_PCM_16BIT }) {
        int bufferSize = AudioRecord.getMinBufferSize(rate, CHANNEL_CONFIG, f);
        if (bufferSize > 0) {
          // TODO : should determine state here (b/c different devices handle different rates)
//          rate = r; // buffer size is valid, Sample rate supported
          Log.i("[StackmatController]", "  supported rate: " + r + " with format " + f);
        }
      }
    }*/
    Log.i("[StackmatController]", "rate is " + rate);
    int minbuffersize = AudioRecord.getMinBufferSize(rate, CHANNEL_CONFIG, AUDIO_FORMAT);
    Log.i("[StackmatController]", "minbuffers ok. minbuffersize: " + minbuffersize);
    audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, rate, CHANNEL_CONFIG, AUDIO_FORMAT, minbuffersize);
    Log.i("[StackmatController]", "start recording");
    audioRecord.startRecording();
    Log.i("[StackmatController]", "started");

    if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
      Log.i("[StackmatController]", "  Wrong state! return");
      return;
    }

//    ByteArrayOutputStream os = new ByteArrayOutputStream();
//    BufferedOutputStream bos = new BufferedOutputStream(os);
//    DataOutputStream dos = new DataOutputStream(bos);

//    try {
      while (true) {
        short[] audioData = new short[minbuffersize];
        int bufferReadResult = audioRecord.read(audioData, 0, minbuffersize);
        if (bufferReadResult < 0) {
          Log.e("[StackmatController]", "Error during read! returned " + bufferReadResult + ". stop!");
          stop = true;
        }

        // conversion to little indian and then to 8-bit (http://stackoverflow.com/questions/2319907/using-audiorecord-with-8-bit-encoding-in-android):
        /*byte[] lens = new byte[minbuffersize];
        for (int i = 0; ( i + 1) < audioData.length; i += 2) {
          lens[i] = audioData[ i + 1];
          lens[i + 1] = audioData[ i];
        }
        for (int i = 1, j = 0; i < audioData.length; i += 2, j++) {
          lens[j] = lens[i];
        }*/

        /*for (int i = 0, j = audioData.length - 1; i < j; i++, j--) { // convert to little endian
          short b = audioData[i];
          audioData[i] = audioData[j];
          audioData[j] = b;
        }*/

        int displayStart = -1;
        StringBuilder sb = new StringBuilder();
        for (int i = 0 ; i < audioData.length ; i++) {
          char c = (char) audioData[i];
          if (c == 'I' || c == 'A' || c == 'S' || c == 'L' || c == 'R' || c == 'C') {
            displayStart = i;
          }
          if (displayStart >= 0 && i < displayStart + 9) {
            sb.append(c).append(' ');
            if (i == displayStart - 8 && c == 0x0A) {
              sb.append("   <------");
            }
          }
          if (i == displayStart + 9) {
            Log.i("[StackmatController]", "sb: " + sb.toString());
            sb = new StringBuilder();
            displayStart = -1;
          }
        }

        /*for (int i = 0 ; i < audioData.length ; i++) {
//          audioData[i] = (short) (audioData[i] >> 8); // to 8-bit
//          (unsigned char)((unsigned short)(input + 32768) >> 8) // to unsigned 8-bit (c++ way)
          // 0x0D : CR    0x0A : LF (2 last characters of the pack of 9)
//          if (audioData[i] == 0x0D && i < audioData.length - 1 && audioData[i+1] == 0x0A) {
          if (((char) audioData[i]) == 0x0D && i < audioData.length - 1 && ((char) audioData[i+1]) == 0x0A) {
//            Log.i("[StackmatController]", "Found CR LF!!!");
            StringBuilder sb1 = new StringBuilder();
            for (int j = Math.max(0, (i+1) - 8); j <= (i+1); j++) {
              sb1.append((char) audioData[j]).append(" ");
            }
            Log.i("[StackmatController]", "chunk: " + sb1.toString());
          }
        }*/

        /*short[] sampleBufferShorts = new short[minbuffersize];
        int numberOfShortsRead = audioRecord.read(sampleBufferShorts, 0, minbuffersize);
        if (numberOfShortsRead != AudioRecord.ERROR_INVALID_OPERATION && numberOfShortsRead != AudioRecord.ERROR_BAD_VALUE) {
          for (int i = 0 ; i < numberOfShortsRead ; ++i) {
            byte b = (byte) ((sampleBufferShorts[i] >> 8) + 128);
            Log.i("[StackmatController]", "b: " + b + " (" + ((char) b) + ")");
          }
        }*/


//        for (int i = 0; i < bufferReadResult; i++) {
//          dos.writeShort(audioData[i]);
//          int eightBit = audioData[i] >> 8;
//          eightBit += eightBit < 0xff && ((audioData[i] & 0xff) > 0x80);
//          if (eightBit > 0) {
//            Log.i("[StackmatController]", "b: " + audioData[i] + " (" + ((char) audioData[i]) + ")");
//          Log.i("[StackmatController]", "b: " + eightBit + " (" + ((char) eightBit) + ")");
//          }
//        }

//      dataList.add(audioData);
        if (stop) {
          audioRecord.stop();
          audioRecord.release();
          synchronized (listSync) {
            listSync.notify();
          }
          break;
        }
      }
//      dos.close();
//    } catch (IOException e) {
//    }
//    byte[] audioBytes = os.toByteArray();
//    for (byte b : audioBytes) {
//      if (b > 0) {
//        Log.i("[StackmatController]", "b: " + b + " (" + ((char) b) + ")");
//      }
//    }
  }

  public void stopRecording() {
    Log.i("[StackmatController]", "stop recording");
    stop = true;
    synchronized (listSync) {
      try {
        listSync.wait(3000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
//    Log.i("[StackmatController]", "bytes: ");
//    for (byte[] bl : dataList) {
//      for (byte b : bl) {
//        Log.i("[StackmatController]", b + " (" + ((char) b) + ")");
//      }
//    }
  }

  /*public void startRecording() {
    new File(FILENAME).delete();
    recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
    recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
    recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
    Log.i("[StackmatController]", "file name: " + FILENAME);
    recorder.setOutputFile(FILENAME);
    try {
      recorder.prepare();
    } catch (IOException e) {
      Log.e("[StackmatController]", "Failed while preparing audio source " + e.getMessage());
    }
    recorder.start();   // Recording is now started
  }

  public void stopRecording() {
    recorder.stop();
    recorder.reset();   // You can reuse the object by going back to setAudioSource() step
    recorder.release(); // Now the object cannot be reused
  }

  public void displayFileContent() {
//    File file = new File(App.INSTANCE.getContext().getFilesDir(), FILENAME);
    File file = new File(FILENAME);
    if (file.exists()) {
      Log.i("[StackmatController]", "file exists at " + file.getAbsolutePath());
      try {
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
        while (bis.available() > 0) {
          Log.i("[StackmatController]", "read " + bis.read());
        }
        Log.i("[StackmatController]", "end");
      } catch (FileNotFoundException e) {
        Log.i("[StackmatController]", "FNFE: " + e.getMessage());
        e.printStackTrace();
      } catch (IOException e) {
        Log.i("[StackmatController]", "IOE: " + e.getMessage());
        e.printStackTrace();
      }
    } else {
      Log.i("[StackmatController]", "file does not exist (" + file.getAbsolutePath() + ")");
    }
    Log.i("[StackmatController]", "content end of file: " + FILENAME);
  }*/

}
