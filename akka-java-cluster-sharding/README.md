# 集群分片

Devices Actor 作为一个温度数值模拟，在集群中构造一个 Device Actor 的分片，并且每秒发送一个模拟温度数值给一个指定 deviceId 的 Device Acotr

关键点

* Device Actor 位置是由集群管理的
* Device Actor 是有状态的，相同 deviceId 的温度会发送给具有这个 deviceId 的Actor