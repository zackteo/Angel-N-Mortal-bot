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

(def writer1 (clojure.java.io/writer "start.log" :append true))

(def writer2 (clojure.java.io/writer "help.log" :append true))

(def writer3 (clojure.java.io/writer "message.log" :append true))

(def writer_angel (clojure.java.io/writer "angel.log" :append true))

(def writer_mortal (clojure.java.io/writer "mortal.log" :append true))

(def writer_error (clojure.java.io/writer "error.log" :append true))

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

(h/defhandler handler

  (h/command-fn "start"
    (fn [{{id :id :as chat} :chat}]
      (println "Bot joined new chat: " chat)
      (clojure.pprint/pprint chat writer1)
      (t/send-text token id "Welcome to angelnmortal_bot!")))

  (h/command-fn "help"
    (fn [{{id :id :as chat} :chat}]
      (println "Help was requested in " chat)
      (clojure.pprint/pprint chat writer2)
      (t/send-text token id "Hello My Fellow Refugee :P\n\n/msg_angel -insert text here- to message your angel\n/msg_mortal -insert text here- to message your mortal\nYou will see \"Sent!\" when your message is sent! :)\n\nDo take this time to get to know someone from our community better! And do be nice and be encouraging - especially given the current covid situation :x\n\nHave fun!!!")))

  (h/command-fn "msg_angel"
    (fn [{{id :id} :chat :as message}]
      (if (:angel ((keyword-ify id) pairing))
        (let [angel-id (:angel ((keyword-ify id) pairing))]
          (println "Sending: " message)
          (clojure.pprint/pprint message writer_angel)
          (try
            (t/send-text token angel-id (clojure.string/replace (:text message) #"/msg_angel " "Message from your Mortal:\n\n"))
            (t/send-text token id "Sent!")
            (catch Exception e
              (println "Error")
              (clojure.pprint/pprint (.toString e) writer_error)              
              (clojure.pprint/pprint (.getStackTrace e) writer_error)
              (t/send-text token id "There was an error here. Message was not sent!")
              (println "Test"))))
        (let [test-id id]
          (println "Intercepted message: " message)
          (clojure.pprint/pprint message writer6)
          (t/send-text token id "I don't do a whole lot ... yet.")))))

  (h/command-fn "msg_mortal"
    (fn [{{id :id} :chat :as message}]
      (if (:mortal ((keyword-ify id) pairing))
        (let [mortal-id (:mortal ((keyword-ify id) pairing))]
          (println "Sending: " message)
          (clojure.pprint/pprint message writer_mortal)
          (try
            (t/send-text token mortal-id (clojure.string/replace (:text message) #"/msg_mortal " "Message from your Angel:\n\n"))
            (t/send-text token id "Sent!")
            (catch Exception e
              (println "Error")
              (clojure.pprint/pprint (.toString e) writer_error)              
              (clojure.pprint/pprint (.getStackTrace e) writer_error)
              (t/send-text token id "There was an error here. Message was not sent!")
              (println "Test"))))
        (let [test-id id]
          (println "Intercepted message: " message)
          (clojure.pprint/pprint message writer6)
          (t/send-text token id "I don't do a whole lot ... yet."))))))

;  (h/message-fn
;    (fn [{{id :id} :chat :as message}]
;      (if (:angel ((keyword-ify id) pairing))
;       (let [angel-id (:angel ((keyword-ify id) pairing))]
;          (t/send-text token id "Sending ...")
;          (t/send-text token angel-id (:text message))
;          (t/send-text token id "sent!"))
;       (let [test-id id]
;         (println "Intercepted message: " message)
;         (clojure.pprint/pprint message writer3)
;         (t/send-text token id "I don't do a whole lot ... yet."))))))      

(defn -main
  [& args]
  (when (str/blank? token)
    (println "Please provde token in TELEGRAM_TOKEN environment variable!")
    (System/exit 1))

  (println "Starting the angelnmortal_bot")
  (<!!(p/start token handler)))
