<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>My Wishlists - SmartGiftList</title>
  <link rel="stylesheet" type="text/css" href="${ctx}/css/base.css">
  <link rel="stylesheet" type="text/css" href="${ctx}/css/pages.css">
  <script src="${ctx}/js/nav.js" defer></script>
  <script src="${ctx}/js/wishlists.js" defer></script>
</head>
<body>

<%@ include file="/WEB-INF/views/partials/header.jspf" %>
<%@ include file="/WEB-INF/views/partials/nav.jspf" %>

<!-- Modal: Connect Shared Wishlist via NetID -->
<div id="connect-wishlist-modal" class="modal-backdrop" aria-hidden="true">
  <div class="modal-card modal-card-compact">
    <h2>Connect Wishlist</h2>
    <div class="section-desc">
      <p>Join a shared wishlist by entering its NetID invite code.</p>
    </div>
    <button type="button" class="modal-close" data-modal-close aria-label="Close">&times;</button>
    <form action="${ctx}/wishlists" method="post" id="connect-wishlist-form" data-preserve-scroll="true">
      <input type="hidden" name="_csrf" value="${csrfToken}">
      <input type="hidden" name="action" value="connect-wishlist">
      <div class="modal-fields-col">
        <label for="connect-netid-input">NetID:</label>
        <input type="text" id="connect-netid-input" name="netIdConnect" placeholder="Enter NetID..." required>
      </div>
      <div class="modal-actions">
        <button type="submit" class="owl-create-submit-button">Connect Wishlist</button>
      </div>
    </form>
  </div>
</div>

<!-- Modal: Create Wishlist -->
<div id="create-wishlist-modal" class="modal-backdrop" aria-hidden="true">
  <div class="modal-card">
    <h2>Create Wishlist</h2>
    <div class="section-desc">
      <p>Enter the details for your new wishlist below.</p>
    </div>
    <button type="button" class="modal-close" data-modal-close aria-label="Close">&times;</button>
    <form action="${ctx}/wishlists" method="post" id="create-wishlist-form" data-preserve-scroll="true">
      <input type="hidden" name="_csrf" value="${csrfToken}">
      <input type="hidden" name="action" value="create-wishlist">
      <div class="modal-form-row">
        <div class="modal-image-col">
          <img id="create-list-preview" class="modal-image-preview"
               src="${ctx}/img/anlaesse/PaperList.webp" alt="Cover preview">
          <div class="owl-create-list-icon-container">
            <label for="create-list-icon">Theme:</label>
            <select name="list_icon" id="create-list-icon" data-preview-target="create-list-preview">
              <c:forEach var="opt" items="${coverOptions}">
                <option value="${opt.relativePath}" data-preview-src="${ctx}/${opt.relativePath}">
                  <c:out value="${opt.label}"/>
                </option>
              </c:forEach>
            </select>
          </div>
        </div>
        <div class="modal-fields-col">
          <label for="create-wishlist-name">Wishlist Title:</label>
          <input type="text" id="create-wishlist-name" name="wishlist_name" required>
          <label for="create-wishlist-occasion">Occasion:</label>
          <input type="text" id="create-wishlist-occasion" name="occasion">
          <label for="create-wishlist-date">Occasion Date:</label>
          <input type="date" id="create-wishlist-date" name="date">
          <label for="create-wishlist-description">Description:</label>
          <textarea id="create-wishlist-description" name="description"></textarea>
          <label for="create-wishlist-links">Reference URL:</label>
          <input type="url" id="create-wishlist-links" name="links" placeholder="https://...">
        </div>
      </div>
      <div class="modal-actions">
        <button type="submit" class="owl-create-submit-button">Create Wishlist</button>
      </div>
    </form>
  </div>
</div>

<!-- Modal: Add Gift -->
<div id="add-gift-modal" class="modal-backdrop" aria-hidden="true">
  <div class="modal-card">
    <h2>Add Gift</h2>
    <div class="section-desc">
      <p>Add a new gift idea to the selected wishlist.</p>
    </div>
    <button type="button" class="modal-close" data-modal-close aria-label="Close">&times;</button>
    <form action="${ctx}/wishlists" method="post" id="add-gift-form" data-preserve-scroll="true">
      <input type="hidden" name="_csrf" value="${csrfToken}">
      <input type="hidden" name="action" value="add-gift">
      <input type="hidden" name="wishlistId" value="${wishlist != null ? wishlist.id : 0}">
      <div class="modal-form-row">
        <div class="modal-image-col">
          <img id="add-gift-preview" class="modal-image-preview"
               src="${ctx}/img/anlaesse/PaperList.webp" alt="Gift image preview">
          <div class="owl-create-list-icon-container">
            <label for="add-gift-icon">Image:</label>
            <select id="add-gift-icon" name="giftIcon" data-preview-target="add-gift-preview">
              <c:forEach var="opt" items="${coverOptions}">
                <option value="${opt.relativePath}" data-preview-src="${ctx}/${opt.relativePath}">
                  <c:out value="${opt.label}"/>
                </option>
              </c:forEach>
            </select>
          </div>
        </div>
        <div class="modal-fields-col">
          <label for="add-gift-name">Gift Name:</label>
          <input type="text" id="add-gift-name" name="giftName" required>

          <label for="add-gift-type">Gift Type:</label>
          <select id="add-gift-type" name="giftType">
            <option value="single">Individual Gift</option>
            <option value="group">Group Gift</option>
          </select>

          <label for="add-gift-people">Participants Needed:</label>
          <input type="number" id="add-gift-people" name="numberOfPeople" min="1" max="100" value="1">

          <label for="add-gift-surprise" class="modal-checkbox-label">
            <input type="checkbox" id="add-gift-surprise" name="surpriseGift"> Surprise Gift
          </label>

          <label for="add-gift-description">Description:</label>
          <textarea id="add-gift-description" name="description"></textarea>

          <label for="add-gift-price">Price (optional):</label>
          <input type="text" id="add-gift-price" name="price" placeholder="19.99">

          <label for="add-gift-link">Product URL (optional):</label>
          <input type="url" id="add-gift-link" name="link" placeholder="https://...">
        </div>
      </div>
      <div class="modal-actions">
        <button type="submit" class="owlpedit-submit-button">Save</button>
      </div>
    </form>
  </div>
</div>

<!-- Modal: Gift Details & Claim/Release -->
<div id="gift-details-modal" class="modal-backdrop"
     data-auto-open="${openGiftDetailsModal ? 'true' : 'false'}"
     aria-hidden="${openGiftDetailsModal ? 'false' : 'true'}">
  <div class="modal-card modal-card-split">
    <button type="button" class="modal-close" data-modal-close aria-label="Close">&times;</button>
    <div class="modal-details-left">
      <h2>Gift Details</h2>
      <c:choose>
        <c:when test="${selectedGift != null}">
          <img id="detail-gift-preview" class="modal-image-preview"
               src="${ctx}/${selectedGift.icon}" alt="Gift preview">
        </c:when>
        <c:otherwise>
          <img id="detail-gift-preview" class="modal-image-preview"
               src="${ctx}/img/anlaesse/PaperList.webp" alt="Gift preview">
        </c:otherwise>
      </c:choose>

      <c:if test="${selectedGift != null}">
        <form action="${ctx}/wishlists" method="post" data-preserve-scroll="true">
          <input type="hidden" name="_csrf" value="${csrfToken}">
          <input type="hidden" name="action" value="claim-gift">
          <input type="hidden" name="giftId" value="${selectedGift.id}">
          <button type="submit" class="btn-claim">Claim</button>
        </form>
        <form action="${ctx}/wishlists" method="post" data-preserve-scroll="true">
          <input type="hidden" name="_csrf" value="${csrfToken}">
          <input type="hidden" name="action" value="release-gift">
          <input type="hidden" name="giftId" value="${selectedGift.id}">
          <button type="submit" class="btn-release">Release</button>
        </form>
      </c:if>

      <ul id="detail-taken-list" class="taken-list">
        <c:forEach var="claimer" items="${dashboard.selectedGiftClaimers}">
          <li class="taken-item">
            <img src="${ctx}/${claimer.avatarPath}" alt="Profile picture">
            <span class="taken-item-name"><c:out value="${claimer.username}"/></span>
          </li>
        </c:forEach>
      </ul>
    </div>

    <div class="modal-details-right modal-fields-col">
      <label for="detail-gift-name">Gift Name:</label>
      <input type="text" id="detail-gift-name" class="readonly-field" placeholder="No gift selected"
             value="<c:out value='${selectedGift.title}'/>" readonly>

      <label for="detail-gift-type">Gift Type:</label>
      <input type="text" id="detail-gift-type" class="readonly-field"
             value="<c:out value='${selectedGift.detailTypeLabel}'/>" readonly>

      <label for="detail-gift-people">Participants Needed:</label>
      <input type="number" id="detail-gift-people" class="readonly-field"
             value="<c:out value='${selectedGift.numberOfPeople}'/>" readonly>

      <label for="detail-gift-description">Description:</label>
      <textarea id="detail-gift-description" class="readonly-field" readonly><c:out value="${selectedGift.description}"/></textarea>

      <label for="detail-gift-price">Price (optional):</label>
      <input type="text" id="detail-gift-price" class="readonly-field"
             value="<c:out value='${selectedGift.formattedPrice}'/>" readonly>

      <label for="detail-gift-link">Product URL (optional):</label>
      <input type="url" id="detail-gift-link" class="readonly-field"
             value="<c:out value='${selectedGift.url}'/>" readonly>
      <c:if test="${selectedGift != null && not empty selectedGift.url}">
        <a href="<c:out value='${selectedGift.url}'/>" target="_blank" rel="noopener noreferrer">Open Product Link</a>
      </c:if>
    </div>
  </div>
</div>

<main class="main-content">
  <div class="main-E1">
    <section class="E2-top1">
      <h1>Wishlists</h1>
      <%@ include file="/WEB-INF/views/partials/flash.jspf" %>
    </section>
    <div class="E2-top1-anfangende"></div>

    <section class="main-body">
      <div class="owl-container">
        <div class="owl-top-area">
          <div class="selector-container">
            <form action="${ctx}/wishlists" method="post" data-preserve-scroll="true">
              <input type="hidden" name="_csrf" value="${csrfToken}">
              <input type="hidden" name="action" value="select-wishlist">
              <select name="netId" id="select-wishlist" aria-label="Active wishlist selector">
                <c:forEach var="w" items="${dashboard.ownedWishlists}">
                  <option value="${w.netId}" <c:if test="${wishlist != null && wishlist.netId == w.netId}">selected</c:if>>
                    <c:out value="${w.id} - ${w.title}"/>
                  </option>
                </c:forEach>
                <c:forEach var="w" items="${dashboard.connectedWishlists}">
                  <option value="${w.netId}" <c:if test="${wishlist != null && wishlist.netId == w.netId}">selected</c:if>>
                    <c:out value="${w.id} - ${w.title} (shared)"/>
                  </option>
                </c:forEach>
              </select>
              <button type="submit" class="button-black select-wishlist">Select</button>
            </form>
          </div>

          <div class="owl-buttons-addwishlist">
            <button type="button" class="button-black create-wishlist" data-modal-open="create-wishlist-modal">
              <img src="${ctx}/img/icons/folder-plus.svg" alt="Create" class="button-icon">Create Wishlist
            </button>
            <button type="button" class="button-black create-wishlist" data-modal-open="connect-wishlist-modal">
              <img src="${ctx}/img/icons/folder-plus.svg" alt="Connect" class="button-icon">Connect Wishlist (NetID)
            </button>
          </div>
        </div>

        <div class="owl-middle-area">
          <div class="owl-left-box">
            <h2>Wishlist Details</h2>
            <div class="section-desc">
              <p>Configure your active wishlist settings and participants.</p>
            </div>

            <div class="owlu-client-details-container">
              <form action="${ctx}/wishlists" method="post" id="update-wishlist-form" data-preserve-scroll="true">
                <input type="hidden" name="_csrf" value="${csrfToken}">
                <input type="hidden" name="wishlistId" value="${wishlist != null ? wishlist.id : 0}">

                <div class="owlu-client-details-content">
                  <div class="owlu-client-details-left">
                    <c:choose>
                      <c:when test="${wishlist != null}">
                        <img src="${ctx}/${wishlist.icon}" id="wishlist-cover-preview" alt="Wishlist cover" class="owlu-gift-image">
                      </c:when>
                      <c:otherwise>
                        <img src="${ctx}/img/anlaesse/PaperList.webp" id="wishlist-cover-preview" alt="Wishlist cover" class="owlu-gift-image">
                      </c:otherwise>
                    </c:choose>
                    <label for="wishlist-cover-select">Icon:</label>
                    <select name="coverIcon" id="wishlist-cover-select" data-preview-target="wishlist-cover-preview"
                            <c:if test="${!isOwner}">disabled</c:if>>
                      <c:forEach var="opt" items="${coverOptions}">
                        <option value="${opt.relativePath}" data-preview-src="${ctx}/${opt.relativePath}"
                                <c:if test="${wishlist != null && wishlist.icon == opt.relativePath}">selected</c:if>>
                          <c:out value="${opt.label}"/>
                        </option>
                      </c:forEach>
                    </select>
                  </div>

                  <div class="owlu-client-details-right">
                    <label for="owlu-wishlist-name">Wishlist Title:</label>
                    <input type="text" id="owlu-wishlist-name" class="owlu-input" placeholder="No wishlist selected"
                           value="<c:out value='${wishlist.title}'/>" readonly>

                    <label for="owlu-event">Occasion:</label>
                    <input type="text" id="owlu-event" name="occasion" placeholder="No occasion specified"
                           value="<c:out value='${wishlist.event}'/>"
                           <c:if test="${!isOwner}">readonly</c:if>>

                    <label for="owlu-date">Occasion Date:</label>
                    <input type="date" id="owlu-date" name="eventDate"
                           value="<c:out value='${wishlist.eventDateIso}'/>"
                           <c:if test="${!isOwner}">readonly</c:if>>
                  </div>
                </div>

                <label for="owlu-description">Description:</label>
                <textarea id="owlu-description" name="description" placeholder="No description provided..."
                          <c:if test="${!isOwner}">readonly</c:if>><c:out value="${wishlist.description}"/></textarea>

                <label for="owlu-links">Reference URL:</label>
                <input type="url" id="owlu-links" name="links" placeholder="No URL provided..."
                       value="<c:out value='${wishlist.url}'/>"
                       <c:if test="${!isOwner}">readonly</c:if>>

                <div class="owlu-net-id-instruction">
                  Share this NetID invite code with friends or family so they can connect to your wishlist:
                </div>

                <div class="owlu-net-id-container">
                  <label for="owlu-net-id">Net-ID:</label>
                  <input type="text" id="owlu-net-id" class="readonly-field owlu-input"
                         value="<c:out value='${wishlist.netId}'/>" readonly>
                </div>

                <div class="owlu-net-id-participants">Current Participants:</div>
                <div class="owlu-participant-table-container">
                  <table class="owlu-participant-table">
                    <thead>
                      <tr>
                        <th></th>
                        <th>Role</th>
                        <th>Name</th>
                      </tr>
                    </thead>
                    <tbody>
                      <c:forEach var="participant" items="${dashboard.participants}">
                        <tr>
                          <td><img src="${ctx}/${participant.avatarPath}" alt="Profile picture" class="owlu-participant-image"></td>
                          <td><c:out value="${participant.owner ? 'Owner' : 'Participant'}"/></td>
                          <td><c:out value="${participant.username}"/></td>
                        </tr>
                      </c:forEach>
                    </tbody>
                  </table>
                </div>

                <c:if test="${wishlist != null}">
                  <div class="owlu-action-buttons">
                    <button type="submit" name="action" value="delete-wishlist" class="owlu-delete-btn">
                      <c:out value="${isOwner ? 'Remove' : 'Leave Wishlist'}"/>
                    </button>
                    <c:if test="${isOwner}">
                      <button type="submit" name="action" value="update-wishlist" class="owlu-edit-btn">Save</button>
                    </c:if>
                  </div>
                </c:if>
              </form>
            </div>
          </div>

          <div class="owl-right-box">
            <h2>Gifts</h2>
            <div class="section-desc">
              <p>Track requested items, group contributions, and surprise gifts.</p>
            </div>

            <div class="owlp-stats-desc" id="owlp-stats-desc">
              <p id="owlp-uv"><img src="${ctx}/img/status/uv.png" alt="status icon"> IG - Individual Gift</p>
              <p id="owlp-gg"><img src="${ctx}/img/status/gg.png" alt="status icon"> GG - Group Gift</p>
              <p id="owlp-sg"><img src="${ctx}/img/status/ug.png" alt="status icon"> SG - Surprise Gift</p>
              <p id="owlp-v"><img src="${ctx}/img/status/v.png" alt="status icon"> CL - Claimed</p>
            </div>

            <div class="owlp-table-container">
              <form action="${ctx}/wishlists" method="post" id="gifts-table-form" data-preserve-scroll="true">
                <input type="hidden" name="_csrf" value="${csrfToken}">
                <input type="hidden" name="wishlistId" value="${wishlist != null ? wishlist.id : 0}">
                <table class="owlp-table">
                  <thead>
                    <tr>
                      <th></th>
                      <th>Name</th>
                      <th>Description</th>
                      <th>Attributes</th>
                      <th>Contributors</th>
                    </tr>
                  </thead>
                  <tbody>
                    <c:forEach var="gift" items="${dashboard.gifts}">
                      <tr data-status="${gift.statusKey}">
                        <td>
                          <input type="checkbox" name="selectedGiftId" value="${gift.id}" class="owlp-select-checkbox"
                                 aria-label="Select <c:out value='${gift.title}'/>">
                        </td>
                        <td><c:out value="${gift.shortTitle}"/></td>
                        <td><c:out value="${gift.shortDescription}"/></td>
                        <td><c:out value="${gift.typeBadges}"/></td>
                        <td>
                          <div class="owlp-profile-images">
                            <c:forEach var="avatarPath" items="${gift.claimerAvatarPaths}">
                              <img src="${ctx}/${avatarPath}" alt="Profile picture" class="owlp-profile-img">
                            </c:forEach>
                          </div>
                        </td>
                      </tr>
                    </c:forEach>
                  </tbody>
                </table>
              </form>
            </div>

            <div class="owlp-button-container">
              <button form="gifts-table-form" type="submit" class="owlp-button" id="owlp-details-btn"
                      name="action" value="gift-details">Details</button>
              <button type="button" class="owlp-button" id="owlp-add-btn"
                      data-modal-open="add-gift-modal">+ Add Gift</button>
              <button form="gifts-table-form" type="submit" class="owlp-button" id="owlp-delete-btn"
                      name="action" value="delete-gift">
                <img src="${ctx}/img/icons/trash3-fill.svg" alt="Delete" class="button-icon">Delete
              </button>
            </div>

            <h2 class="owlp-chatbox-title">Discussion</h2>
            <div class="section-desc">
              <p>Share gift ideas, coordinate surprises, and exchange notes.</p>
            </div>

            <div class="chat-container">
              <div class="chat-messages">
                <c:forEach var="comment" items="${dashboard.comments}">
                  <c:choose>
                    <c:when test="${comment.event}">
                      <div class="message"><c:out value="${comment.text}"/></div>
                    </c:when>
                    <c:otherwise>
                      <div class="message user-message">
                        <div class="user-initials">
                          <img src="${ctx}/${comment.avatarPath}" alt="Profile picture" class="owlp-profile-img">
                        </div>
                        <div class="user-text">
                          <c:if test="${not empty comment.username}">
                            <strong><c:out value="${comment.username}"/>:</strong>
                          </c:if>
                          <c:out value="${comment.text}"/>
                        </div>
                      </div>
                    </c:otherwise>
                  </c:choose>
                </c:forEach>
              </div>
              <div class="chat-input">
                <form action="${ctx}/wishlists" method="post" id="chatinput-form" class="chat-input-form" data-preserve-scroll="true">
                  <input type="hidden" name="_csrf" value="${csrfToken}">
                  <input type="hidden" name="wishlistId" value="${wishlist != null ? wishlist.id : 0}">
                  <input type="text" placeholder="Write a message..." name="commentText">
                  <button type="submit" class="send-btn" name="action" value="send-comment">
                    <img src="${ctx}/img/icons/send-plus-fill.svg" alt="Send"> SEND
                  </button>
                  <button type="submit" class="load-btn" name="action" value="refresh-comments" aria-label="Refresh comments">
                    <img src="${ctx}/img/icons/arrow-clockwise.svg" alt="Refresh">
                  </button>
                  <button type="submit" class="load-btn" name="action" value="withdraw-comment" aria-label="Withdraw last message">
                    <img src="${ctx}/img/icons/trash3-fill.svg" alt="Withdraw last message">
                  </button>
                </form>
              </div>
            </div>
          </div>
        </div>

        <div class="owl-bottom-area">
          <h2 class="owlc-section-title">Overview</h2>
          <div class="section-desc">
            <p>Browse your personal and shared wishlists at a glance.</p>
          </div>

          <h3 class="owlc-section-title">My Wishlists</h3>
          <div class="owlc-card-scroll-container">
            <div class="owlc-card-container">
              <c:forEach var="w" items="${dashboard.ownedWishlists}">
                <div class="owlc-card">
                  <div class="owlc-card-image">
                    <img src="${ctx}/${w.icon}" alt="Wishlist cover">
                  </div>
                  <div class="owlc-card-content">
                    <h3><c:out value="${w.title}"/></h3>
                    <p><c:out value="${w.event}"/></p>
                    <form action="${ctx}/wishlists" method="post" class="owlc-form" data-preserve-scroll="true">
                      <input type="hidden" name="_csrf" value="${csrfToken}">
                      <input type="hidden" name="action" value="select-wishlist">
                      <input type="hidden" name="netId" value="${w.netId}">
                      <button type="submit" class="owlc-select-button">Select</button>
                    </form>
                    <p class="owlc-description"><c:out value="${w.shortDescription}"/></p>
                  </div>
                </div>
              </c:forEach>
              <div class="owlc-card">
                <div class="owlc-card-image"></div>
                <div class="owlc-card-content">
                  <p><button type="button" id="owlc-card-content-add" class="owlc-select-button" data-modal-open="create-wishlist-modal">+ Create</button></p>
                </div>
              </div>
            </div>
          </div>

          <h3 class="owlc-section-title">Shared Wishlists from Friends</h3>
          <div class="owlc-card-scroll-container">
            <div class="owlc-card-container">
              <c:forEach var="w" items="${dashboard.connectedWishlists}">
                <div class="owlc-card">
                  <div class="owlc-card-image">
                    <img src="${ctx}/${w.icon}" alt="Wishlist cover">
                  </div>
                  <div class="owlc-card-content">
                    <h3><c:out value="${w.title}"/></h3>
                    <p><c:out value="${w.event}"/></p>
                    <form action="${ctx}/wishlists" method="post" class="owlc-form" data-preserve-scroll="true">
                      <input type="hidden" name="_csrf" value="${csrfToken}">
                      <input type="hidden" name="action" value="select-wishlist">
                      <input type="hidden" name="netId" value="${w.netId}">
                      <button type="submit" class="owlc-select-button">Select</button>
                    </form>
                    <p class="owlc-description"><c:out value="${w.shortDescription}"/></p>
                  </div>
                </div>
              </c:forEach>
              <div class="owlc-card">
                <div class="owlc-card-image"></div>
                <div class="owlc-card-content">
                  <p><button type="button" id="owlc-card-content-connect" class="owlc-select-button" data-modal-open="connect-wishlist-modal">+ Connect via NetID</button></p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
    <div class="E2-top1-anfangende"></div>
    <section class="E2-end1"></section>
  </div>
</main>

<%@ include file="/WEB-INF/views/partials/footer.jspf" %>

</body>
</html>
