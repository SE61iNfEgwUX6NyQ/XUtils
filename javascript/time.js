//将秒转化为时分秒的格式
function formatTime(second) {
	return [ parseInt(second / 60 / 60), Math.floor(second / 60 % 60), second % 60 ]
			.join(":").replace(/\b(\d)\b/g, "0$1");
}