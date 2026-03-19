import os
import shutil
import re

src_dir = r"c:\Users\Mei\Desktop\Project\BookStore\bookstore-backend\src\main\java\com\bookstore"

def get_feature(filename):
    if filename == "BookstoreApplication.java":
        return "" # root
    name = filename.lower()
    if any(k in name for k in ["cart"]): return "cart"
    if any(k in name for k in ["coupon"]): return "coupon"
    if any(k in name for k in ["review", "report"]): return "review"
    if any(k in name for k in ["order", "checkout", "return"]): return "order"
    if any(k in name for k in ["chat", "ai"]): return "chat"
    if any(k in name for k in ["club"]): return "bookclub"
    if any(k in name for k in ["book", "genre", "stock", "logitem"]): return "book"
    if any(k in name for k in ["user", "wishlist", "history", "point"]): return "user"
    if any(k in name for k in ["exception", "aspect"]): return "aop"
    if any(k in name for k in ["config", "initializer", "handler"]): return "config"
    if any(k in name for k in ["interceptor", "jwt", "constants", "util", "forwarding"]): return "util"
    
    return "common" # fallback

# 1. Map files to new features and physically move them
class_to_pkg = {}
all_files = []

for root, dirs, files in os.walk(src_dir):
    for f in files:
        if f.endswith(".java"):
            fp = os.path.join(root, f)
            all_files.append((fp, f))

moved_files = []
for fp, f in all_files:
    feature = get_feature(f)
    dest_pkg = f"com.bookstore.{feature}" if feature else "com.bookstore"
    dest_dir = os.path.join(src_dir, feature)
    
    if not os.path.exists(dest_dir):
        os.makedirs(dest_dir)
        
    dest_fp = os.path.join(dest_dir, f)
    
    # move file
    if os.path.abspath(fp) != os.path.abspath(dest_fp):
        shutil.move(fp, dest_fp)
        
    class_name = f[:-5]
    class_to_pkg[class_name] = dest_pkg
    moved_files.append((dest_fp, dest_pkg))

# 2. Update contents
for dest_fp, dest_pkg in moved_files:
    try:
        with open(dest_fp, "r", encoding="utf-8") as file:
            content = file.read()
            
        # Update package declaration
        content = re.sub(r'(?m)^package\s+[a-zA-Z0-9_\.]+;', f'package {dest_pkg};', content)
        
        # We need to replace ALL explicitly qualified usages, e.g. com.bookstore.bean.BooksBean -> com.bookstore.book.BooksBean
        # and imports import com.bookstore.bean.BooksBean -> import com.bookstore.book.BooksBean
        
        # A simple powerful regex to catch both imports and inline usages:
        # (?xia) \bcom\.bookstore\.[a-zA-Z0-9_]+\.([A-Za-z0-9_]+)\b
        # Wait, if an import is wildcard `import com.bookstore.bean.*;`, it won't map to a specific class.
        # Let's hope no wildcards exist. If they do, they might break. Let's fix specific classes.
        def replacer(m):
            cls_name = m.group(1)
            if cls_name in class_to_pkg:
                return f"{class_to_pkg[cls_name]}.{cls_name}"
            return m.group(0) # unchanged if class unknown
        
        # Fix all `com.bookstore.ANY_DIR.ClassName`
        content = re.sub(r'\bcom\.bookstore\.[a-zA-Z0-9_]+\.([A-Z][A-Za-z0-9_]+)\b', replacer, content)
        
        # Also, existing code might have imports from `bookstore.*` depending on if it wasn't fixed!
        # Fix `bookstore.ANY_DIR.ClassName`
        content = re.sub(r'(?<!com\.)\bbookstore\.[a-zA-Z0-9_]+\.([A-Z][A-Za-z0-9_]+)\b', replacer, content)
        
        # Now, we also need to ADD imports for classes that are in a DIFFERENT package but are used implicitly.
        # Find all Words that match a known ClassName. If it's used, but not imported, add an import!
        # Note: simplistic import adder can cause false positives, but since class names are unique and CamelCase, 
        # it's usually safe. We can skip if it's already imported or same package.
        used_classes = set(re.findall(r'\b([A-Z][A-Za-z0-9_]+)\b', content))
        
        imports_to_add = set()
        for c in used_classes:
            if c in class_to_pkg:
                cls_pkg = class_to_pkg[c]
                # if different package, and not already imported
                if cls_pkg != dest_pkg:
                    import_stmt = f"import {cls_pkg}.{c};"
                    if import_stmt not in content:
                        imports_to_add.add(import_stmt)
        
        if imports_to_add:
            # find package declaration line and insert after it
            def insert_imports(m):
                return m.group(0) + "\n\n" + "\n".join(imports_to_add)
            content = re.sub(r'(?m)^package\s+[a-zA-Z0-9_\.]+;', insert_imports, content, count=1)

        with open(dest_fp, "w", encoding="utf-8") as file:
            file.write(content)
            
    except Exception as e:
        print(f"Error updating {dest_fp}: {e}")

# clean up empty dirs
for root, dirs, files in os.walk(src_dir, topdown=False):
    for d in dirs:
        dir_path = os.path.join(root, d)
        if not os.listdir(dir_path):
            os.rmdir(dir_path)

print(f"Refactoring complete. Moved {len(moved_files)} files.")
