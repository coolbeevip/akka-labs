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