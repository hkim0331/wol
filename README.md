# wol

Wake local PCs on and off.
A training of programming clojure, multimethods and `pmap`.

## depends
* wakeonlan

## prepare

make ~/clojure/wol.edn as,

```
{"label"
 {:name "host.local"
  :mac  "mac:add:ress:xx:yy:zz"
  :off  "command to shutdown host"}
...
}
```

## usage

    $ wol version -- show version number
    $ wol usage -- show usage
    $ wol list -- list entries in wol.edn
    $ wol on host1 host2 ... -- wake up hosts
    $ wol off host1 host2 ... -- shutdown hosts
    $ wol status host1 host2 ... -- show status

## example

    $ wol list
    $ wol on nuc
    $ wol status nuc m3
    $ wol off nuc
