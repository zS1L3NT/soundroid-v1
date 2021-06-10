import ffmpeg from "fluent-ffmpeg"
import path from "path"
import fs from "fs"
import ytdl from "ytdl-core"
import {v4} from "uuid"

const config = require("../../config.json")

const readyWritePath = (id: string) =>
	path.join(__dirname, "..", "..", "ready", id + ".mp3")
const songsWritePath = (id: string) =>
	path.join(__dirname, "..", "..", "songs", id + ".mp3")

/**
 * Endpoint to convert or wait for a song
 *
 * @param sendToClient
 * @param args
 */
export default async (
	sendToClient: (event: string, tag: string, data: any) => void,
	...args: any[]
) => {
	const [id] = args as string[]
	const TAG = "convert_song[" + v4() + "]:"
	if (!id) return sendToClient("error", id, "Missing id")
	console.time(TAG)
	console.log(TAG)

	// Check if request was sent by mistake
	if (fs.existsSync(songsWritePath(id))) {
		console.log(TAG, `File exists, sending "${id}"`)
		console.timeEnd(TAG)
		return sendToClient("convert_song", id, `${config.songs}/${id}.mp3`)
	}

	let info: ytdl.videoInfo
	const url = "https://youtu.be/" + id
	try {
		console.log(TAG, "Fetching: " + id)
		info = await ytdl.getBasicInfo(url)
		console.log(TAG, "Found   : " + id)
	} catch (e) {
		console.error(TAG, "Invalid YouTube ID")
		console.timeEnd(TAG)
		return sendToClient("error", id, "Invalid YouTube ID")
	}

	const totalSeconds = parseInt(info.videoDetails.lengthSeconds)
	sendToClient("convert_song_downloading", id, "")

	if (fs.existsSync(readyWritePath(id))) {
		console.log(TAG, "File being created, waiting for callback...")
		console.timeEnd(TAG)
		return
	}

	const youtubeStream = ytdl(url, {
		filter: "audioonly",
		quality: "highest"
	})

	console.log(TAG, "File creating...")
	ffmpeg(youtubeStream)
		.audioBitrate(info.formats[0].bitrate!)
		.withAudioCodec("libmp3lame")
		.toFormat("mp3")
		.output(readyWritePath(id))
		.on("progress", progress => {
			const TimeRegex = new RegExp("(\\d\\d):(\\d\\d):(\\d\\d).(\\d\\d)")
			const TimeMatch = progress.timemark.match(TimeRegex)
			if (TimeMatch) {
				const [, hours, minutes, seconds] = TimeMatch
				const currentSeconds =
					parseInt(hours) * 3600 +
					parseInt(minutes) * 60 +
					parseInt(seconds)
				const percent = Math.round(
					100 * (currentSeconds / totalSeconds)
				)
				sendToClient("convert_song_progress", id, percent)
				console.log(TAG, `${progress.timemark} => ${percent}%`)
			}
			else {
				console.log(TAG, progress.timemark)
			}
		})
		.on("end", () => {
			console.log(TAG, "File created, sending to all: " + id)
			console.timeEnd(TAG)
			fs.renameSync(readyWritePath(id), songsWritePath(id))
			sendToClient("convert_song", id, `${config.songs}/${id}.mp3`)
		})
		.on("error", err => {
			console.error(TAG, err)
			console.timeEnd(TAG)
			sendToClient("error", id, `Error converting song on Server`)
		})
		.run()
}
