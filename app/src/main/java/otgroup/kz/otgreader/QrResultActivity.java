package otgroup.kz.otgreader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;
import butterknife.BindView;
import butterknife.ButterKnife;
import otgroup.kz.otgreader.pitcher.RecorderMonitor;
import otgroup.kz.otgreader.pitcher.STFT;

public class QrResultActivity extends AppCompatActivity {
    private static final String TAG = QrResultActivity.class.getSimpleName();
    private static final int FREQ = 5000;

    Timer timer;
    TimerTask timerTask;

    //we are going to use a handler to be able to run in our TimerTask
    final Handler handler = new Handler();

    @BindView(R.id.image_view_qr_result)
    ImageView imageViewQrResult;

    String summ;
    private Looper samplingThread;

    private final static double SAMPLE_VALUE_MAX = 32767.0;   // Maximum signal value
    private final static int RECORDER_AGC_OFF = MediaRecorder.AudioSource.VOICE_RECOGNITION;
    private final static int BYTE_OF_SAMPLE = 2;
    private static int fftLen = 1024;
    private static int sampleRate = 44100;
    private static int audioSourceId = RECORDER_AGC_OFF;
    private static String wndFuncName = "Hanning";
    private boolean isAWeighting = false;
    private boolean bWarnOverrun = true;
    private double timeDurationPref = 4.0;
    private static int nFFTAverage = 2;
    double maxAmpFreq;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_result);

        ButterKnife.bind(this);
        Bitmap bitmap = (Bitmap) getIntent().getExtras().get("QrBitmap");
        imageViewQrResult.setImageBitmap(bitmap);

        summ = getIntent().getExtras().getString("Summ");
//        pitchDetection();

        samplingThread = new Looper();
        samplingThread.start();
    }

    @Override
    protected void onResume() {
        super.onResume();

        //onResume we start our timer so it can start when the app comes from the background
        startTimer();
    }

    public void startTimer() {
        //set a new Timer
        timer = new Timer();
        //initialize the TimerTask's job
        initializeTimerTask();
        //schedule the timer, after the first 5000ms the TimerTask will run every 10000ms
        timer.schedule(timerTask, 1000, 3000); //
    }

    @Override
    protected void onPause() {
        super.onPause();
        stoptimertask();
    }

    public void stoptimertask() {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    public void initializeTimerTask() {

        timerTask = new TimerTask() {
            public void run() {
                //use a handler to run a toast that shows the current timestamp
                handler.post(new Runnable() {
                    public void run() {
                        //get the current timeStamp
//                        Calendar calendar = Calendar.getInstance();
//                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd:MMMM:yyyy HH:mm:ss a");
//                        final String strDate = simpleDateFormat.format(calendar.getTime());

                        String timeStamp = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) + "";

                        Bitmap bitmap = net.glxn.qrgen.android.QRCode.from(summ + "&" + timeStamp).bitmap();
                        imageViewQrResult.setImageBitmap(bitmap);

                        //show the toast
//                        int duration = Toast.LENGTH_SHORT;
//                        Toast toast = Toast.makeText(getApplicationContext(), timeStamp, duration);
//                        toast.show();
                    }
                });
            }
        };
    }



    //another pitcher

    public class Looper extends Thread {
        AudioRecord record;
        volatile boolean isRunning = true;
        volatile boolean isPaused1 = false;
        double wavSecOld = 0;      // used to reduce frame rate
        public STFT stft;   // use with care

        /*DoubleSineGen sineGen1;
        DoubleSineGen sineGen2;
        double[] mdata;*/

        public Looper() {
            /*isPaused1 = ((SelectorText) findViewById(R.id.run)).getText().toString().equals("stop");
            // Signal sources for testing
            double fq0 = Double.parseDouble(getString(R.string.test_signal_1_freq1));
            double amp0 = Math.pow(10, 1/20.0 * Double.parseDouble(getString(R.string.test_signal_1_db1)));
            double fq1 = Double.parseDouble(getString(R.string.test_signal_2_freq1));
            double fq2 = Double.parseDouble(getString(R.string.test_signal_2_freq2));
            double amp1 = Math.pow(10, 1/20.0 * Double.parseDouble(getString(R.string.test_signal_2_db1)));
            double amp2 = Math.pow(10, 1/20.0 * Double.parseDouble(getString(R.string.test_signal_2_db2)));
            if (audioSourceId == 1000) {
                sineGen1 = new DoubleSineGen(fq0, sampleRate, SAMPLE_VALUE_MAX * amp0);
            } else {
                sineGen1 = new DoubleSineGen(fq1, sampleRate, SAMPLE_VALUE_MAX * amp1);
            }
            sineGen2 = new DoubleSineGen(fq2, sampleRate, SAMPLE_VALUE_MAX * amp2);*/
        }

        private void SleepWithoutInterrupt(long millis) {
            try {
                Thread.sleep(millis);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        private double baseTimeMs = SystemClock.uptimeMillis();

        private void LimitFrameRate(double updateMs) {
            // Limit the frame rate by wait `delay' ms.
            baseTimeMs += updateMs;
            long delay = (int) (baseTimeMs - SystemClock.uptimeMillis());
//      Log.i(TAG, "delay = " + delay);
            if (delay > 0) {
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    Log.i(TAG, "Sleep interrupted");  // seems never reached
                }
            } else {
                baseTimeMs -= delay;  // get current time
                // Log.i(TAG, "time: cmp t="+Long.toString(SystemClock.uptimeMillis())
                //            + " v.s. t'=" + Long.toString(baseTimeMs));
            }
        }

        // generate test data
        /*private int readTestData(short[] a, int offsetInShorts, int sizeInShorts, int id) {
            if (mdata == null || mdata.length != sizeInShorts) {
                mdata = new double[sizeInShorts];
            }
            Arrays.fill(mdata, 0.0);
            switch (id - 1000) {
                case 1:
                    sineGen2.getSamples(mdata);
                case 0:
                    sineGen1.addSamples(mdata);
                    for (int i = 0; i < sizeInShorts; i++) {
                        a[offsetInShorts + i] = (short) Math.round(mdata[i]);
                    }
                    break;
                case 2:
                    for (int i = 0; i < sizeInShorts; i++) {
                        a[i] = (short) (SAMPLE_VALUE_MAX * (2.0*Math.random() - 1));
                    }
                    break;
                default:
                    Log.w(TAG, "readTestData(): No this source id = " + audioSourceId);
            }
            LimitFrameRate(1000.0*sizeInShorts / sampleRate);
            return sizeInShorts;
        }*/

        @Override
        public void run() {
            // Wait until previous instance of AudioRecord fully released.
            SleepWithoutInterrupt(500);

            int minBytes = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT);
            if (minBytes == AudioRecord.ERROR_BAD_VALUE) {
                Log.e(TAG, "Looper::run(): Invalid AudioRecord parameter.\n");
                return;
            }

            /**
             * Develop -> Reference -> AudioRecord
             *    Data should be read from the audio hardware in chunks of sizes
             *    inferior to the total recording buffer size.
             */
            // Determine size of buffers for AudioRecord and AudioRecord::read()
            int readChunkSize    = fftLen/2;  // /2 due to overlapped analyze window
            readChunkSize        = Math.min(readChunkSize, 2048);  // read in a smaller chunk, hopefully smaller delay
            int bufferSampleSize = Math.max(minBytes / BYTE_OF_SAMPLE, fftLen/2) * 2;
            // tolerate up to about 1 sec.
            bufferSampleSize = (int)Math.ceil(1.0 * sampleRate / bufferSampleSize) * bufferSampleSize;

            // Use the mic with AGC turned off. e.g. VOICE_RECOGNITION
            // The buffer size here seems not relate to the delay.
            // So choose a larger size (~1sec) so that overrun is unlikely.
            if (audioSourceId < 1000) {
                record = new AudioRecord(audioSourceId, sampleRate, AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT, BYTE_OF_SAMPLE * bufferSampleSize);
            } else {
                record = new AudioRecord(RECORDER_AGC_OFF, sampleRate, AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT, BYTE_OF_SAMPLE * bufferSampleSize);
            }
            /*Log.i(TAG, "Looper::Run(): Starting recorder... \n" +
                    "  source          : " + (audioSourceId<1000?getAudioSourceNameFromId(audioSourceId):audioSourceId) + "\n" +
                    String.format("  sample rate     : %d Hz (request %d Hz)\n", record.getSampleRate(), sampleRate) +
                    String.format("  min buffer size : %d samples, %d Bytes\n", minBytes / BYTE_OF_SAMPLE, minBytes) +
                    String.format("  buffer size     : %d samples, %d Bytes\n", bufferSampleSize, BYTE_OF_SAMPLE*bufferSampleSize) +
                    String.format("  read chunk size : %d samples, %d Bytes\n", readChunkSize, BYTE_OF_SAMPLE*readChunkSize) +
                    String.format("  FFT length      : %d\n", fftLen) +
                    String.format("  nFFTAverage     : %d\n", nFFTAverage));*/
            sampleRate = record.getSampleRate();

            if (record.getState() == AudioRecord.STATE_UNINITIALIZED) {
                Log.e(TAG, "Looper::run(): Fail to initialize AudioRecord()");
                // If failed somehow, leave user a chance to change preference.
                return;
            }

            short[] audioSamples = new short[readChunkSize];
            int numOfReadShort;

            stft = new STFT(fftLen, sampleRate, wndFuncName);
            stft.setAWeighting(isAWeighting);

            RecorderMonitor recorderMonitor = new RecorderMonitor(sampleRate, bufferSampleSize, "Looper::run()");
            recorderMonitor.start();

//      FramesPerSecondCounter fpsCounter = new FramesPerSecondCounter("Looper::run()");

            /*WavWriter wavWriter = new WavWriter(sampleRate);
            boolean bSaveWavLoop = bSaveWav;  // change of bSaveWav during loop will only affect next enter.
            if (bSaveWavLoop) {
                wavWriter.start();
                wavSecRemain = wavWriter.secondsLeft();
                wavSec = 0;
                wavSecOld = 0;
                Log.i(TAG, "PCM write to file " + wavWriter.getPath());
            }*/

            // Start recording
            record.startRecording();

            // Main loop
            // When running in this loop (including when paused), you can not change properties
            // related to recorder: e.g. audioSourceId, sampleRate, bufferSampleSize
            // TODO: allow change of FFT length on the fly.
            while (isRunning) {
                // Read data
                /*if (audioSourceId >= 1000) {
                    numOfReadShort = readTestData(audioSamples, 0, readChunkSize, audioSourceId);
                } else {*/
                    numOfReadShort = record.read(audioSamples, 0, readChunkSize);   // pulling
//                }
                if ( recorderMonitor.updateState(numOfReadShort) ) {  // performed a check
                    if (recorderMonitor.getLastCheckOverrun())
                        notifyOverrun();
                    /*if (bSaveWavLoop)
                        wavSecRemain = wavWriter.secondsLeft();*/
                }
                /*if (bSaveWavLoop) {
                    wavWriter.pushAudioShort(audioSamples, numOfReadShort);  // Maybe move this to another thread?
                    wavSec = wavWriter.secondsWritten();
                    updateRec();
                }*/
                if (isPaused1) {
//          fpsCounter.inc();
                    // keep reading data, for overrun checker and for write wav data
                    continue;
                }

                stft.feedData(audioSamples, numOfReadShort);

                // If there is new spectrum data, do plot
                if (stft.nElemSpectrumAmp() >= nFFTAverage) {
                    // Update spectrum or spectrogram
                    /*final double[] spectrumDB = stft.getSpectrumAmpDB();
                    System.arraycopy(spectrumDB, 0, spectrumDBcopy, 0, spectrumDB.length);
                    update(spectrumDBcopy);*/
//          fpsCounter.inc();

                    stft.calculatePeak();
                    maxAmpFreq = stft.maxAmpFreq;

                    if (maxAmpFreq >= 16900 && maxAmpFreq <= 17100){
                        Log.i(TAG, "Alaaaaarm!");
                        finish();
                    }
//                    maxAmpDB = stft.maxAmpDB;

                    // get RMS
                    /*dtRMS = stft.getRMS();
                    dtRMSFromFT = stft.getRMSFromFT();*/
                }
            }
            Log.i(TAG, "Looper::Run(): Actual sample rate: " + recorderMonitor.getSampleRate());
            Log.i(TAG, "Looper::Run(): Stopping and releasing recorder.");
            record.stop();
            record.release();
            record = null;

            /*if (bSaveWavLoop) {
                Log.i(TAG, "Looper::Run(): Ending saved wav.");
                wavWriter.stop();
                notifyWAVSaved(wavWriter.relativeDir);
            }*/
        }

        long lastTimeNotifyOverrun = 0;
        private void notifyOverrun() {
            if (!bWarnOverrun) {
                return;
            }
            long t = SystemClock.uptimeMillis();
            if (t - lastTimeNotifyOverrun > 6000) {
                lastTimeNotifyOverrun = t;
                /*AnalyzeActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Context context = getApplicationContext();
                        String text = "Recorder buffer overrun!\nYour cell phone is too slow.\nTry lower sampling rate or higher average number.";
                        Toast toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
                        toast.show();
                    }
                });*/
            }
        }

        public void setPause(boolean pause) {
            this.isPaused1 = pause;
        }

        public boolean getPause() {
            return this.isPaused1;
        }

        public void finish() {
            isRunning = false;
            interrupt();
            /*runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setResult(RESULT_OK);
                    finish();
                }
            });*/
            returnBack();
        }
    }

    //another pitcher

    private void startLooper() {
        samplingThread = new Looper();
        if (samplingThread.stft != null) {
            samplingThread.stft.setAWeighting(false); //is dbA
        }
    }

    private void returnBack(){
        setResult(RESULT_OK);
        finish();
    }

}
