import { Animated, Image, StyleSheet, Text, View } from 'react-native';
import React, { useEffect, useRef } from 'react';

export default function Heart() {
	const anim = useRef(new Animated.Value(1));

	useEffect(() => {
		// makes the sequence loop
		Animated.loop(
			// runs given animations in a sequence
			Animated.sequence([
				// increase size
				Animated.timing(anim.current, {
					toValue: 1.5,
					duration: 2000,
					useNativeDriver: true
				}),
				// decrease size
				Animated.timing(anim.current, {
					toValue: 1,
					duration: 2000,
					useNativeDriver: true
				})
			])
		).start();
	}, []);

	return (
		<View style={styles.container}>
			<Animated.View style={{ transform: [{ scale: anim.current }] }}>
				<Image
					source={require('./images/heart.png')}
					style={{ aspectRatio: 1, height: 100, resizeMode: 'contain' }}
				/>
			</Animated.View>
		</View>
	);
}

const styles = StyleSheet.create({
	container: {
		flex: 1,
		justifyContent: 'center',
		alignItems: 'center'
		// padding: 8
	}
});
