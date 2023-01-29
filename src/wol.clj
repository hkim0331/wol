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
(require '[clojure.edn :as edn])

(def ^:private version "0.3.0")

(defn- usage []
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
        (if (= 0 (:exit (shell/sh "ping" host
                            "-c"  (str count)
                            "-t"  (str timeout))))
          ": on"
          ": off"))))
(comment
  (shell/sh "ping" "syno2" "-c" "2" "-t" "2")
  (ping? "syno2")
  (ping? "nuc.local")
  (shell/sh "ping syno2")
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
  (if (ping? host)
    "already on"
    (and
     (shell/sh "wakeonlan" (find-mac host))
     (wakeup? name))))

(comment
  (up "nuc")
  :rcf)

(defn- down [host]
  (if (boolean (re-find #"on" (ping? host)))
    (shell/sh "ssh" host (find-off host))
    "sleeping"))

(comment
  (ping? "nuc")
  (boolean (re-find #"on" (ping? "nuc")))
  (down "syno2.local")
  (down "nuc.local")
  :rcf)

;; FIXME, up/on, down/off
(let [[verb & hosts] *command-line-args*]
  ;; (println "verb:" verb "hosts:" hosts)
  (case verb
    "configs" (println configs) ;; edn?
    "list"    (println (keys configs)) ;; sorted?
    "version" (println version)
    "status"  (mapv #(-> % ping? println) hosts)
    "up"      (doall (pmap up hosts))
    "on"      (doall (pmap up hosts))
    "down"    (doall (pmap down hosts))
    "off"     (doall (pmap down hosts))
    (usage)))
