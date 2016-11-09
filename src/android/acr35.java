package me.stuartphillips.plugins;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.acs.audiojack.AudioJackReader;
import com.acs.audiojack.ReaderException;

import android.media.AudioManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;

import java.lang.Override;
import java.lang.Runnable;
import java.lang.System;
import java.lang.Thread;
import java.util.Locale;

import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;


/**
 * This class allows control of the ACR35 reader sleep state and PICC commands
 */
public class acr35 extends CordovaPlugin {

    private Transmitter transmitter;
    private AudioManager mAudioManager;
    private AudioJackReader mReader;
    private Context mContext;

    private boolean firstRun = true;    /** Is this plugin being initialised? */
    private boolean firstReset = true;  /** Is this the first reset of the reader? */
	private boolean createThread = true; /* Recreate the thread? Nick */
	private int numTries = 0;
	private int cycleLifespan = 50;
	private String instanceID = "NO ID";

    /** APDU command for reading a card's UID */
    private final byte[] apdu = { (byte) 0xFF, (byte) 0xCA, (byte) 0x00, (byte) 0x00, (byte) 0x00 };
    /** Timeout for APDU response (in <b>seconds</b>) */
    private final int timeout = 1;

    /**
     * Converts raw data into a hexidecimal string
     *
     * @param buffer: raw data in the form of a byte array
     * @return a string containing the data in hexidecimal form
     */
    private String bytesToHex(byte[] buffer) {
        String bufferString = "";
        if (buffer != null) {
            for(int i = 0; i < buffer.length; i++) {
                String hexChar = Integer.toHexString(buffer[i] & 0xFF);
                if (hexChar.length() == 1) {
                    hexChar = "0" + hexChar;
                }
                bufferString += hexChar.toUpperCase(Locale.US) + " ";
            }
        }
        return bufferString.trim();
    }

    /**
     * Checks if the device media volume is set to 100%
     *
     * @return true if media volume is at 100%
     */
    /*private boolean maxVolume() {
        int currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);        

        if (currentVolume < maxVolume) {
			mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, 0);
			currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
			if(currentVolume == maxVolume){
				return true;
			} else {
                return false;
            }
        }
        else{
            return true;
        }
    }*/


/*private boolean maxVolume() {
        // Media Volume
        int currentMediaVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int maxMediaVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        // Alarm Volume
        int currentAlarmVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
        int maxAlarmVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
        // DTMF Volume
        int currentDTMFVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_DTMF);
        int maxDTMFVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_DTMF);  
        // Notification Volume
        int currentNotificationVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION);
        int maxNotificationVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION);
        // Ring Volume
        int currentRingVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
        int maxRingVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
        // System Volume
        int currentSystemVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);
        int maxSystemVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);
        // Voice Call Volume
        int currentVoiceVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);
        int maxVoiceVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);

        // Media Volume
        if (currentMediaVolume < maxMediaVolume) {
			mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxMediaVolume, 0);
			currentMediaVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
			if(currentMediaVolume == maxMediaVolume){
				return true;
			} else {
				return false;
			}
        }
        // Alarm Volume
        if (currentAlarmVolume < maxAlarmVolume) {
			mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxAlarmVolume, 0);
			currentAlarmVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
			if(currentAlarmVolume == maxAlarmVolume){
				return true;
			} else {
				return false;
			}
        }
        // DTMF Volume
        if (currentDTMFVolume < maxDTMFVolume) {
			mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxDTMFVolume, 0);
			currentDTMFVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
			if(currentDTMFVolume == maxDTMFVolume){
				return true;
			} else {
				return false;
			}
        }
        // Notification Volume
        if (currentNotificationVolume < maxNotificationVolume) {
			mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxNotificationVolume, 0);
			currentNotificationVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
			if(currentNotificationVolume == maxNotificationVolume){
				return true;
			} else {
				return false;
			}
        }
        // Ring Volume
        if (currentRingVolume < maxRingVolume) {
			mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxRingVolume, 0);
			currentRingVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
			if(currentRingVolume == maxRingVolume){
				return true;
			} else {
				return false;
			}
        }
        // System Volume
        if (currentSystemVolume < maxSystemVolume) {
			mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxSystemVolume, 0);
			currentSystemVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
			if(currentSystemVolume == maxSystemVolume){
				return true;
			} else {
				return false;
			}
        }
        // Voice Call Volume
        if (currentVoiceVolume < maxVoiceVolume) {
			mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVoiceVolume, 0);
			currentVoiceVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
			if(currentVoiceVolume == maxVoiceVolume){
				return true;
			} else {
				return false;
			}
        }

        if(currentMediaVolume==maxMediaVolume && currentAlarmVolume==maxAlarmVolume && currentDTMFVolume==maxDTMFVolume && currentNotificationVolume==maxNotificationVolume && currentRingVolume==maxRingVolume && currentSystemVolume==maxSystemVolume && currentVoiceVolume==maxVoiceVolume){
        	return true;
        } else {
        	return false;
        }
    }*/

    private boolean maxVolume() {
        // Media Volume
        int currentMediaVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int maxMediaVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        // Alarm Volume
        int currentAlarmVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_ALARM);
        int maxAlarmVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
        // DTMF Volume
        int currentDTMFVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_DTMF);
        int maxDTMFVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_DTMF);  
        // Notification Volume
        int currentNotificationVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
        int maxNotificationVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION);
        // Ring Volume
        int currentRingVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_RING);
        int maxRingVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
        // System Volume
        int currentSystemVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
        int maxSystemVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);
        // Voice Call Volume
        int currentVoiceVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
        int maxVoiceVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);

        // Media Volume
        if (currentMediaVolume < maxMediaVolume) {
			mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxMediaVolume, 0);
			currentMediaVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        }
        // Alarm Volume
        if (currentAlarmVolume < maxAlarmVolume) {
			mAudioManager.setStreamVolume(AudioManager.STREAM_ALARM, maxAlarmVolume, 0);
			currentAlarmVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_ALARM);
        }
        // DTMF Volume
        if (currentDTMFVolume < maxDTMFVolume) {
			mAudioManager.setStreamVolume(AudioManager.STREAM_DTMF, maxDTMFVolume, 0);
			currentDTMFVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_DTMF);
        }
        // Notification Volume
        if (currentNotificationVolume < maxNotificationVolume) {
			mAudioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, maxNotificationVolume, 0);
			currentNotificationVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
        }
        // Ring Volume
        if (currentRingVolume < maxRingVolume) {
			mAudioManager.setStreamVolume(AudioManager.STREAM_RING, maxRingVolume, 0);
			currentRingVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_RING);
        }
        // System Volume
        if (currentSystemVolume < maxSystemVolume) {
			mAudioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, maxSystemVolume, 0);
			currentSystemVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
        }
        // Voice Call Volume
        if (currentVoiceVolume < maxVoiceVolume) {
			mAudioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, maxVoiceVolume, 0);
			currentVoiceVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
        }

        if(currentMediaVolume==maxMediaVolume && currentAlarmVolume==maxAlarmVolume && currentDTMFVolume==maxDTMFVolume && currentNotificationVolume==maxNotificationVolume && currentRingVolume==maxRingVolume && currentSystemVolume==maxSystemVolume && currentVoiceVolume==maxVoiceVolume){
        	return true;
        } else {
        	return false;
        }
    }

    /**
     * Sets the ACR35 reader to continuously poll for the presence of a card. If a card is found,
     * the UID will be returned to the Apache Cordova application
     *
     * @param callbackContext: the callback context provided by Cordova
     * @param cardType: the integer representing card type
     */
    private void read(final CallbackContext callbackContext, final int cardType){
        System.out.println("setting up for reading...");
        firstReset = true;
		createThread = true;

        /* If no device is plugged into the audio socket or the media volume is < 100% */
        if(!mAudioManager.isWiredHeadsetOn()){
            /* Communicate to the Cordova application that the reader is unplugged */
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK,
                    "unplugged"));
            return;
        } else if(!maxVolume()) {
            /* Communicate to the Cordova application that the media volume is low */
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK,
                    "low_volume"));
            return;
        }

        /* Set the PICC response APDU callback */
        mReader.setOnPiccResponseApduAvailableListener
                (new AudioJackReader.OnPiccResponseApduAvailableListener() {
                    @Override
                    public void onPiccResponseApduAvailable(AudioJackReader reader,
                                                            byte[] responseApdu) {
                        /* Update the connection status of the transmitter.
							This just says our device is connected (seeing as we had a result). */
                        transmitter.updateStatus(true);
						
						String cardID = bytesToHex(responseApdu);
						
                        /* Send the card UID to the Cordova application */
						if(cardID.length() > 7){
							System.out.println("NFC CARD FOUND: " + cardID);
							PluginResult result = new PluginResult(PluginResult.Status.OK,
                                bytesToHex(responseApdu));
							callbackContext.sendPluginResult(result);
											
							System.out.println("NFC Issuing KILL after succesfull read");
							transmitter.kill();
						} 
                    }
                });

        /* Set the reset complete callback */
        mReader.setOnResetCompleteListener(new AudioJackReader.OnResetCompleteListener() {
            @Override
            public void onResetComplete(AudioJackReader reader) {
                System.out.println("NFC reset complete");

                /* If this is the first reset, the ACR35 reader must be turned off and back on again
                   to work reliably... */
                if(firstReset){
                    cordova.getThreadPool().execute(new Runnable() {
                        public void run() {
                            try{
								/* I have no idea why this thing creates a thread, which seemingly doesn't do much after it's created? */
								System.out.println("NFC first reset complete");
                                /* Set the reader asleep */
                                mReader.sleep();
                                /* Wait one second */
                                Thread.sleep(1000);
                                /* Reset the reader */
                                firstReset = false;
                                mReader.reset();

                            } catch (InterruptedException e) {
                                e.printStackTrace();
                                // TODO: add exception handling
                            }
                        }
                    });
                } else {
                    /* Create a new transmitter for the UID read command */
					
					if(createThread){
						
						if(transmitter != null){
							System.out.println("NFC killing existing transmitter to start a new one...");
							transmitter.kill();
						}
						transmitter = new Transmitter(mReader, mAudioManager, callbackContext, timeout,
								apdu, cardType);
						/* Cordova has its own thread management system */
						cordova.getThreadPool().execute(transmitter);
						
						PluginResult result = new PluginResult(PluginResult.Status.OK, "ready");
						result.setKeepCallback(true);
						callbackContext.sendPluginResult(result);
					}
                }
            }
        });

        System.out.println("NFC reader reset and start");
        mReader.reset();
        mReader.start();
        System.out.println("NFC setup complete");
    }
	
	
	
	
	
	
	
	// Powers our NFC adapter off
	private class PICCPowerOff implements Runnable {
		@Override
		public void run() {
			try{
				if (!mReader.piccPowerOff()) {
					System.out.println("NFC instance " + instanceID + " | - Error powering off PICC");
				} else {
					System.out.println("NFC instance " + instanceID + " | - PICC powered off, powering on...");
					Thread.sleep(100);
					
					new Thread(new PICCPowerOn()).start();
				}
			}
			catch (InterruptedException e) {
				System.out.println("NFC instance " + instanceID + " | Error powering off PICC (catch)");
				e.printStackTrace();
			}
		}
	}
	
	// Powers the NFC adapter back on
	private class PICCPowerOn implements Runnable {
		@Override
		public void run() {
			try{
				if (!mReader.piccPowerOn(3, 1)) {
					System.out.println("NFC instance " + instanceID + " | - Error powering on PICC");
				} else {
					System.out.println("NFC instance " + instanceID + " | - PICC powered on, requesting read...");
					Thread.sleep(100);
					
					new Thread(new PICCRequestRead()).start();
				}
			}catch (InterruptedException e) {
				System.out.println("NFC instance " + instanceID + " | Error powering on PICC (catch)");
				e.printStackTrace();
			}
		}
	}
	
	// Will request the read from our device.
	private class PICCRequestRead implements Runnable {
		@Override
		public void run() {
			if (!mReader.piccTransmit(2, apdu)) {
				System.out.println("NFC instance " + instanceID + " | - Error requesting read");
			} else {
				System.out.println("NFC instance " + instanceID + " | - Read requested");
			}
		}
	}


    /**
     * Sets the ACR35 reader to continuously poll for the presence of a card. If a card is found,
     * the UID will be returned to the Apache Cordova application.
     *
     * @param callbackContext: the callback context provided by Cordova
     * @param cardType: the integer representing card type
     */
    private void singleRead(final CallbackContext callbackContext, final int cardType){
		
		// Generate a new instance ID (just for console logs)
	   DateFormat dateFormat = new SimpleDateFormat("HH_mm_ss");
	   Date date = new Date();
	   instanceID = dateFormat.format(date);
	   System.out.println("NFC");
	   System.out.println("NFC");
	   System.out.println("NFC instance " + instanceID);
	   
	   // Indicate this is the first reset we're doing.
	   firstReset = true;

        /* If no device is plugged into the audio socket or the media volume is < 100% */
        if(!mAudioManager.isWiredHeadsetOn()){
            /* Communicate to the Cordova application that the reader is unplugged */
			System.out.println("NFC instance " + instanceID + " | unplugged");
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK,
                    "unplugged"));
            return;
        } else if(!maxVolume()) {
            /* Communicate to the Cordova application that the media volume is low */
			System.out.println("NFC instance " + instanceID + " | media volume too low");
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK,
                    "low_volume"));
            return;
        }
		

        /* Set the PICC response APDU callback */
        mReader.setOnPiccResponseApduAvailableListener
                (new AudioJackReader.OnPiccResponseApduAvailableListener() {
                    @Override
                    public void onPiccResponseApduAvailable(AudioJackReader reader,
                                                            byte[] responseApdu) {
						
						// Get the card ID in hex.
						String cardID = bytesToHex(responseApdu);
						
                        // A valid card ID is more than 7 characters.
						// If this is longer, it's likely valid. Send to our Javascript module.
						// Javascript will have to recreate the thread, if we carry on now we sit with cache issues.
						// Thread will end after the callback is sent.
						if(cardID.length() > 7){
							System.out.println("NFC instance " + instanceID + " | card found: " + cardID);
							
							PluginResult result = new PluginResult(PluginResult.Status.OK, cardID);
							result.setKeepCallback(true);
							callbackContext.sendPluginResult(result);
						} 
						
						// If not, keep on polling until we're at the end of our cycle.
						else{
							try{
								numTries = numTries + 1;
								if(numTries < cycleLifespan){
									Thread.sleep(100);
									System.out.println("NFC instance " + instanceID + " | - preparing next read (" + numTries + ")");
									new Thread(new PICCPowerOff()).start();
									
									// Let the interface know we're still here.
									PluginResult result = new PluginResult(PluginResult.Status.OK, "ready");
									result.setKeepCallback(true);
									callbackContext.sendPluginResult(result);
								}else{
									System.out.println("NFC instance " + instanceID + " | - end of cycle");
									PluginResult result = new PluginResult(PluginResult.Status.OK, "end_of_cycle");
									callbackContext.sendPluginResult(result);
								}
							}
							catch (InterruptedException e) {
								System.out.println("NFC instance " + instanceID + " | Error powering on PICC (catch)");
								e.printStackTrace();
							}
						}
					
                    }
                });

        /* Set the reset complete callback */
        mReader.setOnResetCompleteListener(new AudioJackReader.OnResetCompleteListener() {
            @Override
            public void onResetComplete(AudioJackReader reader) {
				
				// We have to do a second reset to ensure we get stable readings.
				// Yes, it's a huge waste of time, but it doesn't seem fixable...
                if(firstReset){
					System.out.println("NFC instance " + instanceID + " | 1st reset complete");
					try{
						mReader.sleep();
						Thread.sleep(500);
						firstReset = false;
						mReader.reset();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
                } 
				
				// Start the polling process after the second reset.
				// Also let the interface know we're ready to start scanning.
				else {				
					System.out.println("NFC instance " + instanceID + " | 2nd reset complete, starting reader");
				
					System.out.println("NFC instance " + instanceID + " | starting PICC sequence");
                    //mReader.piccPowerOff();
                    //Thread.sleep(1000);
					System.out.println("NFC instance " + instanceID + " | - Requesting power off");
					new Thread(new PICCPowerOff()).start();
										
					PluginResult result = new PluginResult(PluginResult.Status.OK, "ready");
					result.setKeepCallback(true);
					callbackContext.sendPluginResult(result);
				}
            }
        });
		
		System.out.println("NFC instance " + instanceID + " | starting initial reset");
		System.out.println("NFC instance " + instanceID + " |");
	
		// Prepare the first reset.
		// We'll carry on in the reset completed listener.
		mReader.start();
		mReader.reset();
		
    }

    /**
     * This method acts as the bridge between Cordova and native Android code. The Cordova
     * application will invoke this method from JavaScript
     *
     * @param action: the command sent by the Cordova application
     * @param args: the command arguments sent by the Cordova application
     * @param callbackContext: the callback context provided by Cordova
     * @return a boolean that notifies whether the command execution was successful
     * @throws JSONException
     */
    @Override
    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext)
            throws JSONException {

        /* Class variables require initialisation on first launch */
        if(firstRun){
            /* Context is acquired using cordova.getActivity() */
            mAudioManager = (AudioManager) this.cordova.getActivity().getApplicationContext()
                    .getSystemService(Context.AUDIO_SERVICE);
            mReader = new AudioJackReader(mAudioManager);
            firstRun = false;
        }

        if (action.equals("read")) {
			
			
			if(transmitter != null){
				//System.out.println("NFC killing existing transmitter to start a new one...");
				transmitter.kill();
			}
			
            System.out.println("NFC reading command...");
            /* Use args.getString to retrieve arguments sent by the Cordova application */
            read(callbackContext, Integer.parseInt(args.getString(0)));
            /* Required so that a result can be returned asynchronously from another thread */
            PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
            result.setKeepCallback(true);
            callbackContext.sendPluginResult(result);
            return true;
        } 
		
		else if (action.equals("singleRead")) {
			numTries = 0;
			cycleLifespan = Integer.parseInt(args.getString(1));
			if(cycleLifespan == 0) cycleLifespan = 50;
			
            singleRead(callbackContext, Integer.parseInt(args.getString(0)));
            /* Send a success message back to Cordova */
            //callbackContext.success();
            return true;
        }
		
		else if (action.equals("sleep")) {
            System.out.println("NFC sleeping command...");
            /* Kill the polling thread */
            if(transmitter != null){
                transmitter.kill();
            }
            /* Send a success message back to Cordova */
            callbackContext.success();
            return true;
        }
		
        /* Else, an invalid command was sent */
        else {
            System.out.println("invalid command");
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.INVALID_ACTION));
            return false;
        }
    }

	
    /**
     * Converts the HEX string to byte array.
     * 
     * @param hexString
     *            the HEX string.
     * @return the byte array.
     */
    private byte[] toByteArray(String hexString) {

        byte[] byteArray = null;
        int count = 0;
        char c = 0;
        int i = 0;

        boolean first = true;
        int length = 0;
        int value = 0;

        // Count number of hex characters
        for (i = 0; i < hexString.length(); i++) {

            c = hexString.charAt(i);
            if (c >= '0' && c <= '9' || c >= 'A' && c <= 'F' || c >= 'a'
                    && c <= 'f') {
                count++;
            }
        }

        byteArray = new byte[(count + 1) / 2];
        for (i = 0; i < hexString.length(); i++) {

            c = hexString.charAt(i);
            if (c >= '0' && c <= '9') {
                value = c - '0';
            } else if (c >= 'A' && c <= 'F') {
                value = c - 'A' + 10;
            } else if (c >= 'a' && c <= 'f') {
                value = c - 'a' + 10;
            } else {
                value = -1;
            }

            if (value >= 0) {

                if (first) {

                    byteArray[length] = (byte) (value << 4);

                } else {

                    byteArray[length] |= value;
                    length++;
                }

                first = !first;
            }
        }

        return byteArray;
    }
}
