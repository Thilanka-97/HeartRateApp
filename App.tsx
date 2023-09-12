/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * @format
 */

import React, { useEffect, useRef, useState } from 'react';

import {
	SafeAreaView,
	StyleSheet,
	Text,
	TouchableOpacity,
	View,
	NativeModules,
	DeviceEventEmitter,
	requireNativeComponent,
	HostComponent,
	Dimensions
} from 'react-native';
import Heart from './Heart';

const { HeartRateModule } = NativeModules;
const listeners: any = {};

export const addListener = (eventName: string, callback: any) => {
	listeners[eventName] = DeviceEventEmitter.addListener(eventName, callback);
};

export const removeListener = (eventName: string | number) => {
	if (listeners[eventName]) {
		listeners[eventName].remove();
		delete listeners[eventName];
	}
};

enum StatusTypes {
	IDLE = 'IDLE',
	START = 'START',
	FINISH = 'FINISH',
	RUNNING = 'RUNNING',
	STOP = 'STOP'
}

function App(): JSX.Element {
	const [pulse, setPulse] = useState<string>('__._');
	const [status, setStatus] = useState<StatusTypes>(StatusTypes.IDLE);

	const cameraComponentRef = useRef<any>(null);
	const graphComponentRef = useRef(null);

	let CameraView: HostComponent<unknown> | null = null;

	useEffect(() => {
		setTimeout(() => {
			HeartRateModule.createTextureView().then((textureViewId: number) => {
				handleListeners();

				const TextureView = requireNativeComponent('TextureView');
				console.log('textureViewId', textureViewId);
				cameraComponentRef.current = textureViewId;
				CameraView = TextureView as HostComponent<unknown>;
				console.log('cameraComponentRef', cameraComponentRef);
				console.log('TextureView', TextureView);
				handleStartMeasure();
			});
		}, 2000);
	}, []);

	const handleStartMeasure = () => {
		setPulse('__._');
		HeartRateModule.startMeasure(cameraComponentRef.current);
	};

	const handleListeners = async () => {
		console.log('handleListeners');

		addListener('full_string', (eventData: any) => {
			console.log('Received event data:', eventData);
		});

		addListener('pulse', (eventData: number) => {
			console.log('Received pulse', Number(eventData).toFixed(1));
			setPulse(Number(eventData).toFixed(1));
		});

		addListener('status', (eventData: StatusTypes) => {
			console.log('Received status', eventData);
			setStatus(eventData);
		});

		addListener('onFinish', (eventData: any) => {
			console.log('onFinish Received event data:', eventData);
		});

		console.log(HeartRateModule);
	};

	return (
		<SafeAreaView style={styles.container}>
			<View
				style={{
					flexDirection: 'row',
					justifyContent: 'center',
					alignItems: 'center',
					height: '100%',
					position: 'relative'
				}}
			>
				<View
					style={{
						display: status === StatusTypes.RUNNING ? 'flex' : 'none',
						flexDirection: 'row',
						alignItems: 'center',
						justifyContent: 'center',
						position: 'absolute',
						top: Dimensions.get('screen').height * 0.1,
						width: '80%'
					}}
				>
					<Heart />
				</View>
				<View
					style={{
						display: 'flex',
						flexDirection: 'column',
						gap: 20
					}}
				>
					<View
						style={{
							display: 'flex',
							justifyContent: 'center',
							alignItems: 'center',
							borderWidth: 3,
							borderColor: 'red',
							borderRadius: Dimensions.get('screen').width * 0.6,
							width: Dimensions.get('screen').width * 0.6,
							aspectRatio: 1
						}}
					>
						<View
							style={{
								display: 'flex',
								flexDirection: 'row',
								alignItems: 'center'
							}}
						>
							<Text
								style={{
									fontSize: 100,
									fontWeight: '400'
								}}
							>
								{pulse}
							</Text>
							{/* <Text
								style={{
									fontSize: 60,
									fontWeight: '400'
								}}
							>
								{`.${pulse.split('.')[1][0]}`}
							</Text> */}
						</View>

						<Text
							style={{
								fontSize: 30,
								fontWeight: 'bold',
								color: 'gray'
							}}
						>
							BPM
						</Text>
					</View>
					<View ref={cameraComponentRef} style={styles.textureViewContainer}></View>
				</View>
				<View
					style={{
						display: status === StatusTypes.FINISH ? 'flex' : 'none',
						flexDirection: 'row',
						alignItems: 'center',
						justifyContent: 'center',
						position: 'absolute',
						bottom: Dimensions.get('screen').height * 0.15,
						width: '80%'
					}}
				>
					<TouchableOpacity
						onPress={() => handleStartMeasure()}
						style={styles.measureButton}
					>
						<Text
							style={{
								fontSize: 16,
								color: 'white',
								fontWeight: '600',
								textAlign: 'center'
							}}
						>
							New Measure
						</Text>
					</TouchableOpacity>
				</View>
				{/* <View ref={cameraComponentRef} />
				<View ref={graphComponentRef} /> */}
			</View>
			{/* </ScrollView> */}
		</SafeAreaView>
	);
}

const styles = StyleSheet.create({
	container: {
		flex: 1,
		flexDirection: 'column',
		backgroundColor: 'black'
	},
	preview: {
		flex: 1,
		justifyContent: 'flex-end',
		alignItems: 'center'
	},
	measureButton: {
		color: '#fff',
		backgroundColor: 'red',
		fontWeight: '700',
		padding: 15,
		paddingHorizontal: 20,
		alignSelf: 'center',
		borderRadius: 5,
		zIndex: 1,
		width: '100%'
	},
	textureViewContainer: {
		width: 200, // Set the width as needed
		height: 200, // Set the height as needed
		position: 'absolute', // You can adjust the position as needed
		top: 20, // Adjust the top position as needed
		left: 20 // Adjust the left position as needed
		// Add any other styles you require
	}
});

export default App;
