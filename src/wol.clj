#!/usr/bin/env bb
;;; origin: ${utils}/wol/src/wol.clj
;;;
;;; usage: wol [on|up|off|down|status] host1 host2 ...
;;;        wol list
;;;        wol help
;;;        wol version
;;;
;;; 2022-03-11 did not reflect git. do it again.
;;; 2023-01-24 pmac requires `doall`.
;;; 2023-01-28 update
(require
 '[babashka.process :refer [shell process exec]]
 '[clojure.edn :as edn])

(comment
  (slurp (:out (shell {:dir "/"} "ls")))
  (slurp (:out (shell {:dir "/"} "ls -l")))
  (slurp (:out (shell "uptime")))
  :rcf)

(def ^:private version "0.5.0")

(defn- usage [verb]
  (println "unknown verb:" verb)
  (println "usage: wol [on|off|status] host1 host2 ...")
  (println "       wol [list|help|version]"))

(def configs
  (try
    (edn/read-string
     (slurp (str (System/getenv "HOME")
                 "/.config/wol/wol.edn")))
    (catch Exception e
      (do
        (println (.getMessage e))
        (System/exit 1)))))

(defn- find-mac
  [host]
  (-> (configs host)
      :mac))

(comment
  (find-mac "nuc")
  :rcf)

(defn- find-off
  [host]
  (-> (configs host)
      :off))

(defn- ping?
  "ping to `host` `count` times, wait `timeout` seconds.
   returns true if host alive."
  ([host] (ping? host 1))
  ([host count] (ping? host count 1))
  ([host count timeout]
   (str host
        (if (= 0 (:exit (shell "ping" host
                            "-c"  (str count)
                            "-t"  (str timeout))))
          ": on"
          ": off"))))
(comment
  (shell "ping" "syno2" "-c" "2" "-t" "2")
  (ping? "syno2")
  (ping? "nuc.local")
  (shell "ping syno2")
  )

(defn- wakeup?
  "print '.' until `(ping? host)` returns true."
  [host]
  (loop [resp false]
    (if resp
      (println host "wakes up.")
      (do
        (print ".")
        (flush)
        (recur (ping? host))))))

(defn- up [host]
  ;; (println host)
  (if (boolean (re-find #"on" (ping? host)))
    "already on"
    (and
     (shell "wakeonlan" (find-mac host))
     (wakeup? host))))

(comment
  (boolean (re-find #"on" (ping? "nic")))
  (ping? "nuc")
  (up "nuc")
  :rcf)

(defn- down [host]
  (if (boolean (re-find #"on" (ping? host)))
    (shell "ssh" host (find-off host))
    "sleeping"))

(comment
  (ping? "nuc")
  (boolean (re-find #"on" (ping? "nuc")))
  (down "syno2.local")
  (down "nuc.local")
  :rcf)

;; FIXME, up/on, down/off
(defn -main [& _args]
 (let [[verb & hosts] *command-line-args*]
  ;; (println "verb:" verb "hosts:" hosts)
   (case verb
     "configs" (println configs) ;; edn?
     "list"    (println (keys configs)) ;; sorted?
     "version" (println version)
     "status"  (mapv #(-> % ping? println) hosts)
     "up"      (doall (pmap up hosts))
     "down"    (doall (pmap down hosts))
     "on"      (doall (pmap up hosts))
     "off"     (doall (pmap down hosts))
     (usage verb))))

;; (-main)
