/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * @format
 */

// import 'react-native-reanimated';
// import 'react-native-gesture-handler';
import React, { useEffect, useRef } from 'react';

import {
  SafeAreaView,
  StyleSheet,
  Text,
  TouchableOpacity,
  // TouchableHighlight,
  // StyleSheet,
  // Text,
  View,
  NativeModules,
  DeviceEventEmitter,
  // requireNativeComponent,
  // Dimensions,
  // TextInput,
  // ScrollView,
  findNodeHandle,
} from 'react-native';

const { HeartRateModule } = NativeModules;
const listeners: any = {};

export const addListener = (eventName: string, callback: any) => {
  listeners[eventName] = DeviceEventEmitter.addListener(eventName, callback);
};

// const CameraPreview = requireNativeComponent('CameraPreviewView');

export const removeListener = (eventName: string | number) => {
  if (listeners[eventName]) {
    listeners[eventName].remove();
    delete listeners[eventName];
  }
};

function App(): JSX.Element {
  // const cameraRef = React.useRef<RNCamera | null>(null);
  // const colorSignalRef = React.useRef<number[]>([]);
  // const [torch, setTorch] = useState<'on' | 'off'>('off');

  const myComponentRef = useRef(null);

  const crateCameraPreview = () => {
    const nativeHandle = findNodeHandle(myComponentRef.current);
    if (nativeHandle) {
      HeartRateModule.startMeasure(nativeHandle);
    }
  };

  const takePicture = async () => {
    console.log('takePicture');
    // const message = await HeartRateModule.getMessage();

    addListener('hello', (eventData: any) => {
      // Handle the event data
      console.log('Received event data:', eventData);
    });

    addListener('onFinish', (eventData: any) => {
      // Handle the event data
      console.log('onFinish Received event data:', eventData);
    });

    // HeartRateModule.emitEvent('TEST');
    HeartRateModule.openXmlLayoutActivity();

    console.log(HeartRateModule);
    // console.log(message);
  };

  const testFunc = () => {
    addListener('onFinish', (eventData: any) => {
      // Handle the event data
      console.log('onFinish Received event data:', eventData);
      console.log('onFinish Received event data:', JSON.parse(eventData));
    });
    addListener('onTest', (eventData: any) => {
      // Handle the event data
      console.log('onTest Received event data:', eventData);
      console.log('onTest Received event data:', JSON.parse(eventData));
      JSON.parse(eventData).map((item: any) => {
        console.log('measurement', item.measurement);
        console.log('timestamp', item.timestamp);
      });
    });
    HeartRateModule.testFunction();
  };

  return (
    <SafeAreaView style={styles.container}>
      <View style={{ flexDirection: 'row', justifyContent: 'center', alignItems: 'center', height: '100%' }}>
        {/* <TouchableOpacity
          onPress={() => {
            console.log('torch jkj', torch);
            setTorch((val) => (val === 'off' ? 'on' : 'off'));
          }}
          style={styles.capture}
        >
          <Text style={{ fontSize: 14, color: 'black' }}> Torch </Text>
        </TouchableOpacity> */}
        {/* <View style={{ flex: 1 }}>
          <View style={{ width: Dimensions.get('window').width, height: 59 }} />

          <View style={{ flexDirection: 'row', alignItems: 'center' }}>
            <View style={{ width: 50, height: 50 }} />
            <Text style={{ flex: 1, marginLeft: 5, fontSize: 24, fontWeight: 'bold' }}>Result will appear here</Text>
          </View>

          <ScrollView style={{ flex: 1, marginHorizontal: 5 }}>
            <TextInput
              style={{ flex: 1, height: '100%', borderWidth: 1, borderColor: 'gray' }}
              placeholder="Result will appear here"
              editable={false}
            />
          </ScrollView>
        </View> */}

        <View ref={myComponentRef} />

        <TouchableOpacity onPress={() => takePicture()} style={styles.capture}>
          <Text style={{ fontSize: 14, color: 'black' }}> Open Heart Rate Monitor </Text>
        </TouchableOpacity>
        {/* <TouchableOpacity onPress={() => getMediaDevice()} style={styles.capture}>
          <Text style={{ fontSize: 14, color: 'black' }}> Camera ON </Text>
        </TouchableOpacity> */}

        <TouchableOpacity onPress={() => crateCameraPreview()} style={styles.capture}>
          <Text style={{ fontSize: 14, color: 'black' }}> TEST </Text>
        </TouchableOpacity>
      </View>
      {/* </ScrollView> */}
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    flexDirection: 'column',
    backgroundColor: 'black',
  },
  preview: {
    flex: 1,
    justifyContent: 'flex-end',
    alignItems: 'center',
  },
  capture: {
    flex: 0,
    backgroundColor: '#fff',
    borderRadius: 5,
    padding: 15,
    paddingHorizontal: 20,
    alignSelf: 'center',
    margin: 20,
    zIndex: 1,
  },
});

export default App;
