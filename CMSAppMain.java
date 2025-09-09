package assigment1;
import java.util.*;
class Author {
    protected int authorId;
    protected String name, email, bio, role;
    public Author(int authorId, String name, String email, String bio, String role) {
        this.authorId = authorId; this.name = name; this.email = email; this.bio = bio; this.role = role;
    }
    public int getAuthorId() { return authorId; }
    public String getRole() { return role; }
    public String getName() { return name; }
    public void setBio(String bio) { this.bio = bio; }
    public void setEmail(String email) { this.email = email; }
    public void setRole(String role) { this.role = role; }
    public boolean canPublishDirectly() { return false; }
    public int publicationCount(List<Post> posts) {
        int cnt = 0;
        for (Post p : posts) if (p.authorId == this.authorId && p.status.equals("PUBLISHED")) cnt++;
        return cnt;
    }
}

class StaffWriter extends Author {
    public StaffWriter(int id, String name, String email, String bio) {
        super(id, name, email, bio, "StaffWriter");
    }
    @Override
    public boolean canPublishDirectly() { return true; }
}

class GuestAuthor extends Author {
    public GuestAuthor(int id, String name, String email, String bio) {
        super(id, name, email, bio, "GuestAuthor");
    }
    @Override
    public boolean canPublishDirectly() { return false; }
}


class Post {
    protected int postId, authorId;
    protected String title, content, status; // DRAFT/REVIEW/PUBLISHED
    protected List<String> tags;
    protected Date created, published;
    public Post(int postId, int authorId, String title, String content, List<String> tags) {
        this.postId = postId; this.authorId = authorId; this.title = title; this.content = content;
        this.tags = tags; this.status = "DRAFT"; this.created = new Date(); this.published = null;
    }
    public int getPostId() { return postId; }
    public String getStatus() { return status; }
    public void setTitle(String title) { this.title = title; }
    public void setContent(String content) { this.content = content; }
    public void setStatus(String status) { this.status = status; }
    public void setPublished(Date date) { this.published = date; }
    public boolean hasTag(String tag) { return tags.contains(tag); }
    public String getTitle() { return title; }
    public Date getCreated() { return created; }
}


class Category {
    protected int categoryId;
    protected String name, description;
    protected Category parent;
    public Category(int id, String name, String desc, Category parent) {
        this.categoryId = id; this.name = name; this.description = desc; this.parent = parent;
    }
    public int getCategoryId() { return categoryId; }
    public String getName() { return name; }
    public Category getParent() { return parent; }
    public void setDescription(String desc) { this.description = desc; }
}


class CMSService {
    List<Author> authors = new ArrayList<>();
    List<Post> posts = new ArrayList<>();
    List<Category> categories = new ArrayList<>();
    Map<Integer, List<Integer>> categoryPosts = new HashMap<>();
    public void addAuthor(Author a) { authors.add(a); }
    public void addCategory(Category c) { categories.add(c); categoryPosts.put(c.categoryId, new ArrayList<>()); }
    public Post createPost(int authorId, String title, String content, List<String> tags) {
        int newId = posts.size() + 1;
        Post p = new Post(newId, authorId, title, content, tags);
        posts.add(p); return p;
    }
    public void editPost(int postId, String title, String content) {
        for (Post p : posts) if (p.getPostId() == postId) {
            p.setTitle(title); p.setContent(content); break;
        }
    }
    public void categorizePost(int postId, int categoryId) {
        if (!categoryPosts.containsKey(categoryId)) return;
        categoryPosts.get(categoryId).add(postId);
    }
    public void submitForReview(int postId) {
        for (Post p : posts) if (p.getPostId() == postId && p.getStatus().equals("DRAFT")) p.setStatus("REVIEW");
    }
    public void publish(int postId) {
        for (Post p : posts) {
            if (p.getPostId() == postId && p.getStatus().equals("REVIEW")) {
                Author a = findAuthorById(p.authorId);
                if (a != null && a.canPublishDirectly()) { p.setStatus("PUBLISHED"); p.setPublished(new Date()); }
                else { System.out.println("GuestAuthors need review approval!"); }
            }
        }
    }
    public Author findAuthorById(int id) { for (Author a : authors) if (a.getAuthorId() == id) return a; return null; }

    // Overloaded search methods
    public List<Post> search(String title) {
        List<Post> result = new ArrayList<>();
        for (Post p : posts) if (p.getTitle().contains(title)) result.add(p);
        return result;
    }
    public List<Post> searchByTag(String tag) {
        List<Post> result = new ArrayList<>();
        for (Post p : posts) if (p.hasTag(tag)) result.add(p);
        return result;
    }
    public List<Post> search(Date start, Date end) {
        List<Post> result = new ArrayList<>();
        for (Post p : posts) if (p.getCreated().after(start) && p.getCreated().before(end)) result.add(p);
        return result;
    }
    public List<Post> listByCategory(int categoryId) {
        List<Post> result = new ArrayList<>();
        if (!categoryPosts.containsKey(categoryId)) return result;
        List<Integer> ids = categoryPosts.get(categoryId);
        for (Integer pid : ids) {
            for (Post p : posts) if (p.getPostId() == pid) result.add(p);
        }
        return result;
    }

    public void printCategoryArchive() {
        for (Category c : categories) {
            System.out.println("Category: " + c.getName());
            List<Post> catposts = listByCategory(c.categoryId);
            for (Post p : catposts) System.out.println("\t" + p.getTitle() + " [" + p.getStatus() + "]");
        }
    }
    public void printAuthorPublicationCounts() {
        for (Author a : authors) {
            int count = a.publicationCount(posts);
            System.out.println(a.getName() + ": " + count + " published posts");
        }
    }
}


public class CMSAppMain {
    public static void main(String[] args) {
        CMSService cms = new CMSService();
        // Add authors
        StaffWriter staff = new StaffWriter(1, "Alice", "alice@mail.com", "Senior editor");
        GuestAuthor guest = new GuestAuthor(2, "Bob", "bob@mail.com", "Tech enthusiast");
        cms.addAuthor(staff); cms.addAuthor(guest);

        // Add categories
        Category tech = new Category(1, "Tech", "Technology articles", null);
        Category lifestyle = new Category(2, "LifeStyle", "Life tips", null);
        cms.addCategory(tech); cms.addCategory(lifestyle);

        // Create/edit posts
        Post p1 = cms.createPost(1, "Intro to Java", "Java basics...", Arrays.asList("java","programming"));
        Post p2 = cms.createPost(2, "Travel Tips", "Pack light...", Arrays.asList("travel","tips"));
        cms.categorizePost(p1.getPostId(), tech.getCategoryId());
        cms.categorizePost(p2.getPostId(), lifestyle.getCategoryId());
        cms.editPost(p2.getPostId(), "Ultimate Travel Tips", "Always pack light!");

        // Workflow
        cms.submitForReview(p1.getPostId());
        cms.submitForReview(p2.getPostId());
        cms.publish(p1.getPostId());  // StaffWriter can publish
        cms.publish(p2.getPostId());  // GuestAuthor denied direct publishing

        // Print category archive and author-wise counts
        cms.printCategoryArchive();
        cms.printAuthorPublicationCounts();
    }
}
