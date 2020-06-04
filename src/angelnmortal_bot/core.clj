(ns angelnmortal-bot.core
  (:require [clojure.core.async :refer [<!!]]
            [clojure.string :as str]
            [environ.core :refer [env]]
            [morse.handlers :as h]
            [morse.polling :as p]
            [morse.api :as t])
  (:gen-class))

; TODO: fill correct token
(def token (env :telegram-token))

(def writer_angel (clojure.java.io/writer "angel.log" :append true))

(def writer_mortal (clojure.java.io/writer "mortal.log" :append true))

(def writer_testing (clojure.java.io/writer "testing.log" :append true))

(def writer6 (clojure.java.io/writer "ignore.log" :append true))

; list of telegram id and their corresponding angel n mortal
(def pairing
  {:xxxxxxxxx {:angel xxxxxxxxx :mortal xxxxxxxxx}        
   :xxxxxxxxx {:angel xxxxxxxxx :mortal xxxxxxxxx}      
   :xxxxxxxxx {:angel xxxxxxxxx :mortal xxxxxxxxx}     
   :xxxxxxxxx {:angel xxxxxxxxx :mortal xxxxxxxxx}})    

(defn keyword-ify [id]
  "Takes an int/string and return a keyword"
  (keyword (str id)))

(defn angel-mortal [state id]
  (let [id (keyword-ify id)]
  (if (id @state) (:angel (id pairing)) (:mortal (id pairing)))))


;;Handlers
(defn text-handler [message id]
  (let [id (angel-mortal state id)]
    (println "Sending: " message)
    (t/send-text token id (:text message))))

(defn state-handler [message id]
  (println (str (:username (:from message)) " changing state"))
  (swap! state update-in [(keyword-ify id)] not)
  (if ((keyword-ify id) @state)
    (t/send-text token id "Messages will be sent to your angel now ..")
    (t/send-text token id "Messages will be sent to your mortal now ..")))

(defn sticker-handler [message id]
  (t/send-text token id "I don't do stickers yet"))  
;  (let [fid (->> message :sticker :thumb :file_id) filename (str fid ".tgs")]
;    (t/download-file token fid)
;    (h/message-fn "Sending Sticker")
;    (t/send-sticker token id (clojure.java.io/as-file filename))))

(defn photo-handler [message id]
  (let [fid (-> message :photo last :file_id) filename (str fid ".png")
        id (angel-mortal state id)]
    (t/download-file token fid)
    (h/message-fn "Sending Photo")
    (t/send-photo token id (clojure.java.io/as-file filename))))
  
(defn video-handler [message id]
  (t/send-text token id "I don't do video yet"))
;    (let [fid (->> message :video :thumb :file_id) filename (str fid ".mp4")]
;      (t/download-file token fid)
;      (h/message-fn "Sending Video")
;      (t/send-video token id (clojure.java.io/as-file filename))))

(defn audio-handler [message id]
  (t/send-text token id "I don't do audio yet"))
;    (let [fid (->> message :audio :thumb :file_id) filename (str fid ".mp3")]
;      (t/download-file token fid)
;      (h/message-fn "Sending Audio")
;      (t/send-audio token id (clojure.java.io/as-file filename))))

(defn document-handler [message id]
  (t/send-text token id "I don't do documents yet"))
;    (let [fid (->> message :document :file_id) filename (:file_name (:document message))]
;      (t/download-file token fid)
;      (h/message-fn "Sending Document")
;      (t/send-document token id (clojure.java.io/as-file filename))))

(defn voice-handler [message id]
  (t/send-text token id "I don't support voice note"))

(defn video-note-handler [message id]
  (t/send-text token id "I don't support video note"))

(defn rest-handler [message id]
  (t/send-text token id "I don't support this??"))


;; Rest
(defn start-handler [message id]
  (println "Bot joined new chat: " message)
;  (clojure.pprint/pprint message writer1)
  (t/send-text token id "Welcome to angelnmortal_bot!"))


(defn help-handler [message id]
  (println "Help was requested in " message)
  (t/send-text token id "Hello My Fellow Refugee :P\n\n/msg_angel -insert text here- to message your angel\n/msg_mortal -insert text here- to message your mortal\nYou will see \"Sent!\" when your message is sent! :)\n\nDo take this time to get to know someone from our community better! And do be nice and be encouraging - especially given the current situation :x\n\nHave fun!!!"))

(defn msg-angel-handler [message id]
      (if (:angel ((keyword-ify id) pairing))
        (let [angel-id (:angel ((keyword-ify id) pairing))]
          (println "Sending: " message)
          (clojure.pprint/pprint message writer_angel)
          (t/send-text token angel-id (clojure.string/replace (:text message) #"/msg_angel " "Message from your Mortal:\n\n"))
          (t/send-text token id "Sent!"))
        (let [test-id id]
          (println "Intercepted message: " message)
          (clojure.pprint/pprint message writer6)
          (t/send-text token id "I don't do a whole lot ... yet."))))

(defn msg-mortal-handler [message id]
  (if (:mortal ((keyword-ify id) pairing))
    (let [mortal-id (:mortal ((keyword-ify id) pairing))]
      (println "Sending: " message)
      (clojure.pprint/pprint message writer_mortal)
      (t/send-text token mortal-id (clojure.string/replace (:text message) #"/msg_mortal " "Message from your Angel:\n\n"))
      (t/send-text token id "Sent!"))
    (let [test-id id]
      (println "Intercepted message: " message)
      (clojure.pprint/pprint message writer6)
      (t/send-text token id "I don't do a whole lot ... yet."))))



(defn dispatch-message [message id]
  (cond
    (contains? message :text) (cond
                                (clojure.string/includes? (:text message) "/start") (start-handler message id)
                                (clojure.string/includes? (:text message) "/help") (help-handler message id)
                                (clojure.string/includes? (:text message) "/swap") (state-handler message id)
                                (clojure.string/includes? (:text message) "/msg_angel") (msg-angel-handler message id)
                                (clojure.string/includes? (:text message) "/msg_mortal") (msg-mortal-handler message id)
                                :else (text-handler message id)) ;; why isit not coming here????
    (contains? message :sticker) (sticker-handler message id)
    (contains? message :photo) (photo-handler message id)
    (contains? message :video) (video-handler message id)
    (contains? message :audio) (audio-handler message id)
    (contains? message :document) (document-handler message id)
    (contains? message :voice) (voice-handler message id)
    (contains? message :video_note) (video-note-handler message id)
    :else (rest-handler message id)))


(h/defhandler handler

  (h/message-fn
   (fn [{{id :id} :chat :as message}]
     (try
       (dispatch-message message id)
       (catch Exception e
         (println "Something wrong happened")
         (t/send-text token id "Error: Something wrong happened"))))))


(defn -main
  [& args]
  (when (str/blank? token)
    (println "Please provde token in TELEGRAM_TOKEN environment variable!")
    (System/exit 1))

  (while true
    (println "Starting the angelnmortal_bot")
    (<!!(p/start token handler))))
