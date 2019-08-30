# Stateful Actor Cluster

* Stateful Actor
* Persistence Actor 
* Cluster Sharding
* High Availability

![image-20190830135523054](assets/image-20190830135523054.png)



## 持久化

使用 Redis 持久化插件，需要定期手动清理

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





 