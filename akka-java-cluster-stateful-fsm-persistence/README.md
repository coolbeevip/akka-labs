# 有状态 Actor 在集群中的使用

* 有状态 Actor
* 持久化 Actor 
* 集群分片
* 高可用

![image-20190830135523054](assets/image-20190830135523054.png)



## 持久化清理

使用 Redis 持久化插件，需要定期手动清理已终止的Actor的数据

清理命令

```bash
redis-cli --eval akka-persistence-redis-clean.lua
```

akka-persistence-redis-clean.lua 脚本如下

```lua
local ids = redis.call('smembers','journal:persistenceIds');
local delkeys = {};
for k, v in pairs(ids) do
  local jpid = 'journal:persisted:' .. v;
  local jpidnr = 'journal:persisted:' .. v .. ':highestSequenceNr';
  local hasjpid  = redis.call('exists',jpid);
  if(hasjpid == 0)
  then
    local hasjpidnr  = redis.call('exists',jpidnr);
    if(hasjpidnr == 1)
    then
      redis.call('del',jpidnr);
      redis.call('srem','journal:persistenceIds',v);
      table.insert(delkeys,jpid);
    else
      redis.call('srem','journal:persistenceIds',v);
      table.insert(delkeys,jpid);
    end
  end
end
return delkeys;
```

## 重复消费

* Kafka 消费可能存在重复情况，Actor 要自己判断处理

 