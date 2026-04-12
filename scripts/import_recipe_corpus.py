#!/usr/bin/env python3
"""
Import recipe_corpus_full.json (1.55M recipes, JSONL) into recipe_corpus table.
Streams file line-by-line, batch INSERTs 5000 at a time.
Auto-categorizes by keywords.
"""
import json
import sys
import time
import pymysql

DB_CONFIG = {
    'host': 'bj-cynosdbmysql-grp-arpuqyre.sql.tencentcdb.com',
    'port': 23593,
    'user': 'library',
    'password': 'Library0',
    'database': 'nutriai',
    'charset': 'utf8mb4',
    'connect_timeout': 30,
    'read_timeout': 120,
    'write_timeout': 120,
}

BATCH_SIZE = 5000

CATEGORY_RULES = [
    ('BREAKFAST', ['早餐', '早饭', '早点', '粥', '豆浆', '包子', '馒头', '煎饼', '油条', '吐司']),
    ('SOUP', ['汤', '汤羹', '炖汤', '煲汤', '靓汤']),
    ('DESSERT', ['甜品', '甜点', '蛋糕', '曲奇', '饼干', '布丁', '慕斯', '烘焙', '冰淇淋', '奶茶']),
    ('SNACK', ['小吃', '零食', '小食', '点心']),
    ('DINNER', ['晚餐', '晚饭', '夜宵', '宵夜']),
    ('LUNCH', ['午餐', '午饭', '便当']),
    ('STAPLE', ['主食', '面食', '面条', '饺子', '馄饨', '炒饭', '炒面', '拌面']),
]

def classify_category(keywords, name, dish):
    text = ' '.join(keywords) + ' ' + name + ' ' + dish
    for cat, terms in CATEGORY_RULES:
        for term in terms:
            if term in text:
                return cat
    if '沙拉' in text:
        return 'LUNCH'
    if '饮品' in text or '果汁' in text or '奶昔' in text:
        return 'SNACK'
    if '凉菜' in text:
        return 'LUNCH'
    return 'OTHER'

INSERT_SQL = """INSERT INTO recipe_corpus 
    (name, dish, description, ingredients_json, steps_json, keywords_json, category, author) 
    VALUES (%s, %s, %s, %s, %s, %s, %s, %s)"""

def get_connection():
    return pymysql.connect(**DB_CONFIG)

def main():
    input_file = '/www/wwwroot/ai-based-healthy-diet/recipe_corpus_full.json'
    
    conn = get_connection()
    cur = conn.cursor()
    
    batch = []
    total = 0
    errors = 0
    skipped = 0
    start_time = time.time()
    
    with open(input_file, 'r', encoding='utf-8') as f:
        for line_num, line in enumerate(f, 1):
            try:
                r = json.loads(line.strip())
                name = (r.get('name', '') or '')[:300]
                if not name:
                    skipped += 1
                    continue
                
                dish = (r.get('dish', '') or '')[:200]
                if dish == 'Unknown':
                    dish = ''
                description = r.get('description', '') or ''
                ingredients = r.get('recipeIngredient', []) or []
                steps = r.get('recipeInstructions', []) or []
                keywords = r.get('keywords', []) or []
                author = (r.get('author', '') or '')[:100]
                
                if not ingredients and not steps:
                    skipped += 1
                    continue
                
                category = classify_category(keywords, name, dish)
                
                ing_json = json.dumps(ingredients, ensure_ascii=False)
                steps_json = json.dumps(steps, ensure_ascii=False)
                kw_json = json.dumps(keywords[:10], ensure_ascii=False)[:1000]
                
                batch.append((name, dish, description[:5000], ing_json, steps_json, kw_json, category, author))
                
            except (json.JSONDecodeError, Exception) as e:
                errors += 1
                if errors <= 5:
                    print(f"Parse error line {line_num}: {e}", file=sys.stderr)
                continue
            
            if len(batch) >= BATCH_SIZE:
                try:
                    cur.executemany(INSERT_SQL, batch)
                    conn.commit()
                except Exception as e:
                    conn.rollback()
                    # Insert one by one to skip bad rows
                    for row in batch:
                        try:
                            cur.execute(INSERT_SQL, row)
                            conn.commit()
                        except Exception:
                            errors += 1
                            conn.rollback()
                
                total += len(batch)
                batch = []
                
                if total % 50000 == 0:
                    elapsed = time.time() - start_time
                    rate = total / elapsed
                    print(f"[{time.strftime('%H:%M:%S')}] {total:,} imported ({rate:.0f}/s, {elapsed:.0f}s, skip={skipped}, err={errors})")
                    sys.stdout.flush()
    
    if batch:
        try:
            cur.executemany(INSERT_SQL, batch)
            conn.commit()
            total += len(batch)
        except Exception as e:
            print(f"Final batch error: {e}", file=sys.stderr)
            errors += len(batch)
    
    elapsed = time.time() - start_time
    print(f"\nDONE: {total:,} imported in {elapsed:.0f}s ({total/elapsed:.0f}/s), skipped={skipped}, errors={errors}")
    
    cur.close()
    conn.close()

if __name__ == '__main__':
    main()
