select ag.attr_group_name,ag.attr_group_id,ar.attr_id,
pav.attr_name,pav.attr_value,pav.spu_id
from pms_attr_group ag
left join pms_attr_attrgroup_relation ar
on ag.attr_group_id=ar.attr_group_id
left join pms_product_attr_value pav
on pav.attr_id=ar.attr_id
where catelog_id=225 and spu_id=27


select info.sku_id,
val.attr_id,
val.attr_name,
GROUP_CONCAT(DISTINCT val.attr_value)
from pms_sku_info info
left join pms_sku_sale_attr_value val
on info.sku_id=val.sku_id
where info.spu_id=27
group by val.attr_id,val.attr_name